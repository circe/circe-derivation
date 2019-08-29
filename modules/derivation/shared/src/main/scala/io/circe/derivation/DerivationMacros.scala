package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder }

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) extends ScalaVersionCompat {
  import c.universe._

  private[this] val encoderSymbol: Symbol = c.symbolOf[Encoder.type]
  private[this] val decoderSymbol: Symbol = c.symbolOf[Decoder.type]
  private[this] val encoderTC: Type = typeOf[Encoder[_]].typeConstructor
  private[this] val decoderTC: Type = typeOf[Decoder[_]].typeConstructor
  private[this] val defaultDiscriminator =
    c.Expr[Discriminator](q"_root_.io.circe.derivation.Discriminator.default")
  private[this] val trueExpression = c.Expr[Boolean](q"true")

  private[this] def failWithMessage(message: String): Nothing = c.abort(c.enclosingPosition, message)

  private[this] case class Instance(tc: Type, tpe: Type, name: TermName) {
    def resolve(): Tree = c.inferImplicitValue(appliedType(tc, List(tpe))) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"Could not find $tc instance for $tpe")
      case instance  => instance
    }
  }

  private[this] case class Instances(tpe: Type, encoder: Instance, decoder: Instance)
  private[this] case class Member(
    name: TermName,
    decodedName: String,
    tpe: Type,
    keyName: Option[Tree],
    default: Option[Tree],
    noDefaultValue: Boolean
  )

  private[this] object Member {
    final def fromSymbol(tpe: Type, defaults: ListMap[String, Option[Tree]])(
      memberPosition: (Symbol, Int)
    ): Member = {
      val (sym, position) = memberPosition
      val memberName = sym.name
      val memberDecl = tpe.decl(memberName)

      val default = {
        val value = memberName.decodedName.toString
        if (defaults.contains(value)) defaults(value) else None
      }

      if (!memberDecl.isMethod)
        failWithMessage(
          s"No method $memberName in $tpe (this is probably because a constructor parameter isn't a val)"
        )

      //we extract annotation names
      var keyName: Option[Tree] = None
      var noDefault = false

      sym.annotations.foreach { ann =>
        ann.tree match {
          case Apply(Select(myType2, _), List(value)) =>
            // using string: ugly but fast
            myType2.toString().split('.').last match {
              case "JsonKey" =>
                var realValue = value.toString
                realValue = realValue.substring(1, realValue.length - 1)
                if (realValue.toString.isEmpty)
                  c.abort(
                    c.enclosingPosition,
                    s"Invalid empty key in $tpe.$sym!"
                  )
                keyName = Some(value)
              case "JsonNoDefault" =>
                noDefault = true
              case _ =>
              // we skip other annotations
            }
          case extra =>
            extra.toString().split('.').last match {
              case "JsonNoDefault()" =>
                noDefault = true
              case _ =>
              // we skip other annotations
            }
        }
      }

      Member(
        memberName.toTermName,
        memberName.decodedName.toString,
        memberDecl.asMethod.returnType.asSeenFrom(tpe, tpe.typeSymbol),
        keyName,
        default,
        noDefault
      )
    }
  }

  private[this] sealed abstract class ProductRepr {
    protected[this] def instantiate(params: List[List[Tree]]): Tree

    def instantiate: Tree = instantiate(paramListsWithNames.map(_.map(p => q"${p._2}")))
    def instantiateAccumulating: Tree = instantiate(
      paramListsWithNames.map(
        _.map {
          case (Member(_, _, tpe, _, _, _), name) => extractFromValid(name, tpe)
        }
      )
    )

    val tpe: Type
    val paramLists: List[List[Member]]

    val paramListsWithNames: List[List[(Member, TermName)]] =
      paramLists
        .foldLeft((List.empty[List[(Member, TermName)]], 1)) {
          case ((acc, i), paramList) =>
            val nextParamList = paramList.zipWithIndex.map {
              case (member, j) => (member, TermName(s"res${i + j}"))
            }

            (nextParamList :: acc, i + nextParamList.size)
        }
        ._1
        .reverse

    val instances: List[Instances] =
      paramLists.flatten.zipWithIndex
        .foldLeft(List.empty[Instances]) {
          case (acc, (Member(_, _, tpe, _, _, _), i)) if acc.find(_.tpe =:= tpe).isEmpty =>
            val instances = Instances(
              tpe,
              Instance(encoderTC, tpe, TermName(s"encoder$i")),
              Instance(decoderTC, tpe, TermName(s"decoder$i"))
            )

            instances :: acc
          case (acc, _) => acc
        }
        .reverse

    private[this] def fail(tpe: Type): Nothing = c.abort(c.enclosingPosition, s"Invalid instance lookup for $tpe")

    def encoder(tpe: Type): Instance = instances.find(_.tpe =:= tpe).getOrElse(fail(tpe)).encoder
    def decoder(tpe: Type): Instance = instances.find(_.tpe =:= tpe).getOrElse(fail(tpe)).decoder
  }
  private[this] case class ProductReprWithApply(tpe: Type, paramLists: List[List[Member]]) extends ProductRepr {
    protected[this] def instantiate(params: List[List[Tree]]): Tree = q"${tpe.typeSymbol.companion}.apply(...$params)"
  }
  private[this] case class ProductReprWithConstr(tpe: Type, paramLists: List[List[Member]]) extends ProductRepr {
    protected[this] def instantiate(params: List[List[Tree]]): Tree = q"new $tpe(...$params)"
  }

  private[this] val applyName: TermName = TermName("apply")

  private[this] def membersFromCompanionApply(tpe: Type): Option[ProductRepr] = tpe.companion.decl(applyName) match {
    case NoSymbol => None
    case s =>
      val defaults = caseClassFieldsDefaults(tpe)
      s.alternatives.collect {
        case m: MethodSymbol => m.paramLists
      }.sortBy(-_.map(_.size).sum).headOption.map { applyParams =>
        //We use zipwithIndex for gathering default field value if available
        ProductReprWithApply(
          tpe,
          applyParams.map(_.zipWithIndex.map(Member.fromSymbol(tpe, defaults)))
        )
      }
  }

  private[this] def membersFromPrimaryConstr(tpe: Type): Option[ProductRepr] =
    if (tpe.typeSymbol.isAbstract) {
      None
    } else {
      val defaults = caseClassFieldsDefaults(tpe)
      tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor && m.isPublic && !m.isAbstract =>
          ProductReprWithConstr(
            tpe,
            m.paramLists.map(
              _.zipWithIndex.map(Member.fromSymbol(tpe, defaults))
            )
          )
      }
    }

  private[this] def productRepr(tpe: Type): Option[ProductRepr] =
    membersFromPrimaryConstr(tpe).orElse(membersFromCompanionApply(tpe))

  private[this] def fail(tpe: Type): Nothing = c.abort(
    c.enclosingPosition,
    s"Could not identify primary constructor for $tpe"
  )

  private[this] def checkValSafety(owner: Symbol)(tree: Tree): Boolean = tree match {
    case q"$f(...$x)" if x.nonEmpty => checkValSafety(owner)(f) && x.forall(_.forall(checkValSafety(owner)))
    case x if x.isTerm              => x.symbol.owner == owner
    case x                          => false
  }

  private[this] def checkEncoderValSafety(tree: Tree): Boolean = checkValSafety(encoderSymbol)(tree)
  private[this] def checkDecoderValSafety(tree: Tree): Boolean = checkValSafety(decoderSymbol)(tree)

  private[this] val resName: TermName = TermName("res")

  private[this] def extractFromRight(value: c.TermName, tpe: c.Type): c.Tree = {
    import c.universe._

    q"$value.asInstanceOf[_root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe]].${rightValueName(c)}"
  }

  private[this] def castLeft(value: TermName, tpe: Type): Tree =
    q"$value.asInstanceOf[_root_.io.circe.Decoder.Result[$tpe]]"

  private[this] def extractFromValid(value: TermName, tpe: Type): Tree =
    q"""
      $value.asInstanceOf[
        _root_.cats.data.Validated.Valid[$tpe]
      ].a
    """

  def materializeDecoder[T: c.WeakTypeTag]: c.Expr[Decoder[T]] =
    materializeDecoderImpl[T](None, trueExpression, defaultDiscriminator)

  def materializeEncoder[T: c.WeakTypeTag]: c.Expr[Encoder.AsObject[T]] =
    materializeEncoderImpl[T](None, defaultDiscriminator)

  def materializeCodec[T: c.WeakTypeTag]: c.Expr[Codec.AsObject[T]] =
    materializeCodecImpl[T](None, trueExpression, defaultDiscriminator)

  def materializeDecoderWithTransformMemberNames[T: c.WeakTypeTag](
    transformMemberNames: c.Expr[String => String],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Decoder[T]] =
    materializeDecoderImpl[T](
      Some(transformMemberNames),
      useDefaults,
      discriminator
    )

  def materializeEncoderWithTransformMemberNames[T: c.WeakTypeTag](
    transformMemberNames: c.Expr[String => String],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Encoder.AsObject[T]] =
    materializeEncoderImpl[T](Some(transformMemberNames), discriminator)

  def materializeCodecWithTransformMemberNames[T: c.WeakTypeTag](
    transformMemberNames: c.Expr[String => String],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Codec.AsObject[T]] =
    materializeCodecImpl[T](
      Some(transformMemberNames),
      useDefaults,
      discriminator
    )

  private[this] def materializeCodecImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Codec.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    val subclasses = tpe.typeSymbol.asClass.knownDirectSubclasses
    if (subclasses.isEmpty) {
      materializeCodecCaseClassImpl[T](
        transformMemberNames,
        useDefaults,
        discriminator
      )
    } else {
      materializeCodecTraitImpl[T](
        transformMemberNames,
        useDefaults,
        discriminator,
        subclasses
      )
    }
  }

  private[this] def materializeDecoderImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    val subclasses = tpe.typeSymbol.asClass.knownDirectSubclasses
    if (subclasses.isEmpty) {
      materializeDecoderCaseClassImpl[T](transformMemberNames, useDefaults)
    } else {
      materializeDecoderTraitImpl[T](
        transformMemberNames,
        subclasses,
        discriminator
      )
    }
  }

  private[this] def materializeDecoderTraitImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    subclasses: Set[Symbol],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    expandDiscriminator(discriminator.tree) match {
      case _root_.io.circe.derivation.Discriminator.Embedded(fieldName) =>
        val instanceDefs: List[Tree] = subclasses.map { s =>
          val value =
            Literal(Constant(s.asClass.name.decodedName.toString.toLowerCase()))

          cq""" $value => _root_.io.circe.Decoder[${s.asType}]"""
        }.toList

        c.Expr[Decoder[T]](q"""
            for {
                visitorType <- _root_.io.circe.Decoder[String].prepare(_.downField($fieldName))
                value <- visitorType match {
                  case ..$instanceDefs
                }
              } yield value

          """)
      case _root_.io.circe.derivation.Discriminator.TypeDiscriminator =>
        val instanceDefs: List[Tree] = subclasses.map { s =>
          val value =
            Literal(Constant(s.asClass.name.decodedName.toString.toLowerCase()))

          cq""" $value => c.get(keys.head)(_root_.io.circe.Decoder[${s.asType}]).asInstanceOf[ _root_.io.circe.Decoder.Result[$tpe]]"""
        }.toList

        val result = q"""
           new _root_.io.circe.Decoder[$tpe] {
                 override def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = {
                   val keys=c.keys.map(_.toList).getOrElse(Nil)
                   if(keys.isEmpty){
                     Left(_root_.io.circe.DecodingFailure("Missing type field discriminator for trait field", Nil))
                   } else {
                     keys.head match {
                       case ..$instanceDefs
                     }
                   }
                 }
               }
          """
        c.Expr[Decoder[T]](result)
    }

  }

  private[this] def materializeDecoderCaseClassImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    useDefaults: c.Expr[Boolean]
  ): c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    val globalUseDefaults: Boolean = extractUseDefaults(useDefaults.tree)

    def transformName(name: String): Tree =
      transformMemberNames.fold[Tree](q"$name")(f => q"$f($name)")

    productRepr(tpe).fold(fail(tpe)) { repr =>
      if (repr.paramLists.flatten.isEmpty) {
        c.Expr[Decoder[T]](
          q"""
            new _root_.io.circe.Decoder[$tpe] {
              final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] =
                if (c.focus.exists(_.isObject)) {
                  new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](new $tpe())
                } else {
                  new _root_.scala.Left[_root_.io.circe.DecodingFailure, $tpe](
                    _root_.io.circe.DecodingFailure(${tpe.typeSymbol.name.decodedName.toString}, c.history)
                  )
                }
            }
          """
        )
      } else {

        val instanceDefs: List[Tree] = repr.instances.map(_.decoder).map {
          case instance @ Instance(_, instanceType, name) =>
            val resolved = instance.resolve()

            if (checkDecoderValSafety(resolved)) {
              q"private[this] val $name: _root_.io.circe.Decoder[$instanceType] = $resolved"
            } else {
              q"private[this] def $name: _root_.io.circe.Decoder[$instanceType] = $resolved"
            }
        }

        val reversed = repr.paramListsWithNames.flatten.reverse

        def decode(member: Member): Tree = {
          val realFieldName =
            member.keyName.getOrElse(transformName(member.decodedName))
          q"this.${repr.decoder(member.tpe).name}.tryDecode(c.downField($realFieldName))"

        }

        val last: Tree = q"""
          {
            val $resName: _root_.io.circe.Decoder.Result[${reversed.head._1.tpe}] =
              ${decode(reversed.head._1)}

            if ($resName.isRight) {
              val ${reversed.head._2}: ${reversed.head._1.tpe} =
                ${extractFromRight(resName, reversed.head._1.tpe)}

              new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](
                ${repr.instantiate}
              ): _root_.io.circe.Decoder.Result[$tpe]
            } else ${castLeft(resName, tpe)}
          }
        """

        val result: Tree = reversed.tail.foldLeft(last) {
          case (acc, (member @ Member(_, _, memberType, _, _, _), resultName)) => q"""
            {
              val $resName: _root_.io.circe.Decoder.Result[$memberType] = ${decode(member)}

              if ($resName.isRight) {
                val $resultName: $memberType = ${extractFromRight(resName, memberType)}

                $acc
              } else ${castLeft(resName, tpe)}
            }
          """
        }

        def accumulatingDecode(member: Member): Tree = {
          val realFieldName =
            member.keyName.getOrElse(transformName(member.decodedName))
          if (globalUseDefaults && member.default.isDefined) {
            q"""if(c.downField($realFieldName).isInstanceOf[_root_.io.circe.FailedCursor]) {
              _root_.cats.data.Validated.Valid(${member.default.get})
              } else ${repr.decoder(member.tpe).name}.tryDecodeAccumulating(c.downField($realFieldName)
            )"""
          } else {
            q"""${repr.decoder(member.tpe).name}.tryDecodeAccumulating(c.downField($realFieldName)
            )"""
          }
        }

        val (results: List[Tree], resultNames: List[TermName]) =
          reversed.reverse.map {
            case (member, resultName) =>
              (
                q"""
              val $resultName: _root_.io.circe.Decoder.AccumulatingResult[${member.tpe}] =
                ${accumulatingDecode(member)}
            """,
                resultName
              )
          }.unzip

        val resultErrors: List[Tree] = resultNames.map { resultName =>
          q"errors($resultName)"
        }

        val resultAccumulating: Tree = q"""
          {
            ..$results

            val dfs: _root_.scala.List[_root_.io.circe.DecodingFailure] = List(..$resultErrors).flatten

            if (dfs.isEmpty) {
              _root_.cats.data.Validated.valid[
                _root_.cats.data.NonEmptyList[_root_.io.circe.DecodingFailure],
                $tpe
              ](${repr.instantiateAccumulating})
            } else {
              _root_.cats.data.Validated.invalid[
                _root_.cats.data.NonEmptyList[_root_.io.circe.DecodingFailure],
                $tpe
              ](_root_.cats.data.NonEmptyList.fromListUnsafe[_root_.io.circe.DecodingFailure](dfs))
            }
          }
        """

        c.Expr[Decoder[T]](
          q"""
            new _root_.io.circe.Decoder[$tpe] {
              ..$instanceDefs

              final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = $result

              private[this] def errors(
                result: _root_.io.circe.Decoder.AccumulatingResult[_]
              ): _root_.scala.List[_root_.io.circe.DecodingFailure] = result match {
                case _root_.cats.data.Validated.Valid(_)   => _root_.scala.Nil
                case _root_.cats.data.Validated.Invalid(e) => e.toList
              }

              final override def decodeAccumulating(
                c: _root_.io.circe.HCursor
              ): _root_.io.circe.Decoder.AccumulatingResult[$tpe] = $resultAccumulating
            }
          """
        )
      }
    }
  }

  // we materialize case classes
  private[this] def materializeEncoderCaseClassImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]]
  ): c.Expr[Encoder.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    def transformName(name: String): Tree = transformMemberNames.fold[Tree](q"$name")(f => q"$f($name)")

    productRepr(tpe).fold(fail(tpe)) { repr =>
      val instanceDefs: List[Tree] = repr.instances.map(_.encoder).map {
        case instance @ Instance(_, instanceType, name) =>
          val resolved = instance.resolve()

          if (checkEncoderValSafety(resolved)) {
            q"private[this] val $name: _root_.io.circe.Encoder[$instanceType] = $resolved"
          } else {
            q"private[this] def $name: _root_.io.circe.Encoder[$instanceType] = $resolved"
          }
      }

      // we check if we want to serialize no defaults
      val hasNoDefault = repr.paramLists.flatten.exists(_.noDefaultValue)

      if (hasNoDefault) {
        // we manage default serialization
        val fields: List[Tree] = repr.paramLists.flatten.map {
          case Member(
              name,
              decodedName,
              tpe,
              keyName,
              defaultValue,
              noSerializeDefault
              ) =>
            repr.encoder(tpe) match {
              case Instance(_, _, instanceName) =>
                val realName = keyName.getOrElse(transformName(decodedName))

                if (noSerializeDefault && defaultValue.isDefined) {
                  q"""
                    if(a.$name==${defaultValue.get}) _root_.scala.None else
                      _root_.scala.Some(_root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                        $realName, $instanceName.apply(a.$name)
                    ))"""
                } else {
                  q"""
                    _root_.scala.Some(_root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                      $realName, $instanceName.apply(a.$name)
                    ))"""

                }
            }
        }

        val result = q"""
            new _root_.io.circe.Encoder.AsObject[$tpe] {
              ..$instanceDefs

              final def encodeObject(a: $tpe): _root_.io.circe.JsonObject =
                _root_.io.circe.JsonObject.fromIterable($fields.flatten)
            }
          """
        c.Expr[Encoder.AsObject[T]](result)

      } else {
        // we manage without default serialization - common case

        val fields: List[Tree] = repr.paramLists.flatten.map {
          case Member(name, decodedName, tpe, keyName, _, _) =>
            repr.encoder(tpe) match {
              case Instance(_, _, instanceName) =>
                val realName = keyName.getOrElse(transformName(decodedName))
                q"""
              _root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                ${transformName(decodedName)},
                this.$instanceName.apply(a.$name)
              )
            """
            }
        }

        c.Expr[Encoder.AsObject[T]](
          q"""
          new _root_.io.circe.Encoder.AsObject[$tpe] {
            ..$instanceDefs

            final def encodeObject(a: $tpe): _root_.io.circe.JsonObject =
              _root_.io.circe.JsonObject.fromIterable($fields)
          }
        """
        )
      }
    }
  }

  // we materialize trait
  private[this] def materializeEncoderTraitImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    subclasses: Set[Symbol],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Encoder.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    val instanceDefs: List[Tree] =
      expandDiscriminator(discriminator.tree) match {
        case _root_.io.circe.derivation.Discriminator.Embedded(fieldName) =>
          subclasses.map { s =>
            val subTpe = s.asClass.toType

            cq"""obj : $subTpe => obj.asJsonObject.add($fieldName, ${Literal(
              Constant(s.asClass.name.decodedName.toString.toLowerCase())
            )}.asJson)"""
          }.toList

        case _root_.io.circe.derivation.Discriminator.TypeDiscriminator =>
          subclasses.map { s =>
            val subTpe = s.asClass.toType

            cq"""obj : $subTpe => _root_.io.circe.JsonObject(${Literal(
              Constant(s.asClass.name.decodedName.toString.toLowerCase())
            )} ->
              _root_.io.circe.Json.fromJsonObject(obj.asJsonObject)
            )"""
          }.toList
      }
    c.Expr[Encoder.AsObject[T]](q"""
      {
        import io.circe.syntax._
        new _root_.io.circe.Encoder.AsObject[$tpe] {
           override def encodeObject(a: $tpe): _root_.io.circe.JsonObject = a match {
             case ..$instanceDefs
          }
        }
      }
      """)
  }

  private[this] def materializeEncoderImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Encoder.AsObject[T]] = {
    val tpe = weakTypeOf[T]
    // we manage to work on ADT lets check the subclasses
    val subclasses = tpe.typeSymbol.asClass.knownDirectSubclasses
    if (subclasses.isEmpty) {
      materializeEncoderCaseClassImpl[T](transformMemberNames)
    } else {
      materializeEncoderTraitImpl[T](
        transformMemberNames,
        subclasses,
        discriminator
      )
    }
  }

  // Function to extract default values from case class.
  // We need to extract from companion otherwise they are no collected
  def caseClassFieldsDefaults(
    tpe: Type
  ): ListMap[String, Option[Tree]] =
    if (tpe.companion == NoType) {
      ListMap()
    } else {
      try {
        tpe.companion.member(TermName("apply")).asTerm.alternatives.find(_.isSynthetic) match {
          case None => ListMap()
          case Some(syntatic) =>
            ListMap(
              syntatic.asMethod.paramLists.flatten.zipWithIndex.map {
                case (field, i) =>
                  (
                    field.name.toTermName.decodedName.toString, {
                      val method = TermName(s"apply$$default$$${i + 1}")
                      tpe.companion.member(method) match {
                        case NoSymbol => None
                        case _        => Some(q"${tpe.typeSymbol.companion}.$method")
                      }
                    }
                  )
              }: _*
            )
        }
      } catch {
        case ex: Throwable =>
          ListMap()
      }

    }

  private[this] val withDiscriminatorRegex =
    """.*\.withDiscriminatorName\("(.*)"\).*""".r

  private[this] def expandDiscriminator(tree: Tree): Discriminator =
    tree match {
      case q"io.circe.derivation.annotations.Configuration.default.discriminator" =>
        Discriminator.default
      case other =>
        other.toString() match {
          case withDiscriminatorRegex(name) => Discriminator.Embedded(name)
          case s: String if s.contains("withTypeDiscriminator") =>
            Discriminator.TypeDiscriminator
          case _ =>
            Discriminator.default
        }
    }

  private[this] def materializeCodecTraitImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator],
    subclasses: Set[Symbol]
  ): c.Expr[Codec.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    def transformName(name: String): Tree =
      transformMemberNames.fold[Tree](q"$name")(f => q"$f($name)")

    expandDiscriminator(discriminator.tree) match {
      case _root_.io.circe.derivation.Discriminator.Embedded(fieldName) =>
        val decoderInstanceDefs: List[Tree] = subclasses.map { s =>
          val value =
            Literal(Constant(s.asClass.name.decodedName.toString.toLowerCase()))

          cq""" $value => _root_.io.circe.Decoder[${s.asType}].apply(c).map(_.asInstanceOf[$tpe])"""
        }.toList

        val encoderInstanceDefs: List[Tree] = subclasses.map { s =>
          val subTpe = s.asClass.toType

          cq"""obj : $subTpe => obj.asJsonObject.add($fieldName, ${Literal(
            Constant(s.asClass.name.decodedName.toString.toLowerCase())
          )}.asJson)"""
        }.toList

        val result = q"""
            new _root_.io.circe.Codec.AsObject[$tpe] {
               import io.circe.syntax._
               override def encodeObject(a: $tpe): _root_.io.circe.JsonObject = a match {
                 case ..$encoderInstanceDefs
               }


             override def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = {
                def processDiscriminator(name:String):_root_.io.circe.Decoder.Result[$tpe]={
                  name match {
                    case ..$decoderInstanceDefs
                 }
               }
               c.downField($fieldName).as[String].flatMap(t => processDiscriminator(t))
              }
              }
            """
        c.Expr[Codec.AsObject[T]](result)
      case _root_.io.circe.derivation.Discriminator.TypeDiscriminator =>
        val decoderInstanceDefs: List[Tree] = subclasses.map { s =>
          val value =
            Literal(Constant(s.asClass.name.decodedName.toString.toLowerCase()))

          cq""" $value => c.get(keys.head)(_root_.io.circe.Decoder[${s.asType}]).map(_.asInstanceOf[$tpe])"""
        }.toList

        val encoderInstanceDefs: List[Tree] = subclasses.map { s =>
          val subTpe = s.asClass.toType

          cq"""obj : $subTpe => _root_.io.circe.JsonObject(${Literal(
            Constant(s.asClass.name.decodedName.toString.toLowerCase())
          )} ->
              _root_.io.circe.Json.fromJsonObject(obj.asJsonObject)
            )"""
        }.toList

        val result = q"""
                   new _root_.io.circe.Codec.AsObject[$tpe] {
                      import io.circe.syntax._
                      override def encodeObject(a: $tpe): _root_.io.circe.JsonObject = a match {
                        case ..$encoderInstanceDefs
                     }

                   override def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = {
                     val keys=c.keys.map(_.toList).getOrElse(Nil)
                     if(keys.isEmpty){
                       Left(_root_.io.circe.DecodingFailure("Missing type field discriminator for trait field", Nil))
                     } else {
                       keys.head match {
                         case ..$decoderInstanceDefs
                       }
                     }
                   }
                 }
          """
        c.Expr[Codec.AsObject[T]](result)
    }

  }

  private[this] def extractUseDefaults(useDefaults: Tree): Boolean =
    useDefaults match {
      case q"true "  => true
      case q"false " => false
      case q"io.circe.derivation.annotations.Configuration.default.useDefaults " =>
        true
      case q"Configuration.default.useDefaults "                                                          => true
      case q"Configuration.default.withSnakeCaseMemberNames.useDefaults "                                 => true
      case q"io.circe.derivation.annotations.Configuration.default.withSnakeCaseMemberNames.useDefaults " => true
      case q"Configuration.default.withKebabCaseMemberNames.useDefaults "                                 => true
      case q"io.circe.derivation.annotations.Configuration.default.withKebabCaseMemberNames.useDefaults " => true
      case q"Configuration.decodeOnly.useDefaults "                                                       => true
      case q"io.circe.derivation.annotations.Configuration.decodeOnly.useDefaults "                       => true
      case other if other.toString().endsWith(".useDefaults")                                             => true // hack for namespaces
    }

  private[this] def materializeCodecCaseClassImpl[T: c.WeakTypeTag](
    transformMemberNames: Option[c.Expr[String => String]],
    useDefaults: c.Expr[Boolean],
    discriminator: c.Expr[Discriminator]
  ): c.Expr[Codec.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    // Valid only in macro!!
    val globalUseDefaults: Boolean = extractUseDefaults(useDefaults.tree)

    def transformName(name: String): Tree =
      transformMemberNames.fold[Tree](q"$name")(f => q"$f($name)")

    productRepr(tpe).fold(fail(tpe)) { repr =>
      if (repr.paramLists.flatten.isEmpty) {
        c.Expr[Codec.AsObject[T]](
          q"""
            new _root_.io.circe.Codec.AsObject[$tpe] {
              final def encodeObject(a: $tpe): _root_.io.circe.JsonObject = _root_.io.circe.JsonObject.empty

              final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] =
                if (c.focus.exists(_.isObject)) {
                  new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](new $tpe())
                } else {
                  new _root_.scala.Left[_root_.io.circe.DecodingFailure, $tpe](
                    _root_.io.circe.DecodingFailure(${tpe.typeSymbol.name.decodedName.toString}, c.history)
                  )
                }
            }
          """
        )
      } else {

        val encoderInstanceDefs: List[Tree] = repr.instances.map(_.encoder).map {
          case instance @ Instance(_, instanceType, name) =>
            val resolved = instance.resolve()

            if (checkEncoderValSafety(resolved)) {
              q"private[this] val $name: _root_.io.circe.Encoder[$instanceType] = $resolved"
            } else {
              q"private[this] def $name: _root_.io.circe.Encoder[$instanceType] = $resolved"
            }
        }

        // we manage default serialization
        val fields: List[Tree] = repr.paramLists.flatten.map {
          case Member(
              name,
              decodedName,
              tpe,
              keyName,
              defaultValue,
              noSerializeDefault
              ) =>
            repr.encoder(tpe) match {
              case Instance(_, _, instanceName) =>
                val realName = keyName.getOrElse(transformName(decodedName))

                if (noSerializeDefault && defaultValue.isDefined) {
                  q"""
                    if(a.$name==${defaultValue.get}) _root_.scala.None else
                      _root_.scala.Some(_root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                        $realName, $instanceName.apply(a.$name)
                    ))"""
                } else {
                  q"""
                    _root_.scala.Some(_root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                      $realName, $instanceName.apply(a.$name)
                    ))"""

                }
            }
        }

        val decoderInstanceDefs: List[Tree] = repr.instances.map(_.decoder).map {
          case instance @ Instance(_, instanceType, name) =>
            val resolved = instance.resolve()

            if (checkDecoderValSafety(resolved)) {
              q"private[this] val $name: _root_.io.circe.Decoder[$instanceType] = $resolved"
            } else {
              q"private[this] def $name: _root_.io.circe.Decoder[$instanceType] = $resolved"
            }
        }

        val reversed = repr.paramListsWithNames.flatten.reverse

        def decode(member: Member): Tree = {
          val realFieldName =
            member.keyName.getOrElse(transformName(member.decodedName))
          if (globalUseDefaults && member.default.isDefined) {
            q"""if(c.downField($realFieldName).isInstanceOf[_root_.io.circe.FailedCursor]) {
              Right(${member.default.get})
              } else ${repr.decoder(member.tpe).name}.tryDecode(c.downField($realFieldName)
            )"""
          } else {
            q"""${repr.decoder(member.tpe).name}.tryDecode(c.downField($realFieldName)
            )"""
          }
        }

        val last: Tree = q"""
          {
            val $resName: _root_.io.circe.Decoder.Result[${reversed.head._1.tpe}] =
              ${decode(reversed.head._1)}

            if ($resName.isRight) {
              val ${reversed.head._2}: ${reversed.head._1.tpe} =
                ${extractFromRight(resName, reversed.head._1.tpe)}

              new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](
                ${repr.instantiate}
              ): _root_.io.circe.Decoder.Result[$tpe]
            } else ${castLeft(resName, tpe)}
          }
        """

        val result: Tree = reversed.tail.foldLeft(last) {
          case (acc, (member @ Member(_, _, memberType, _, _, _), resultName)) => q"""
            {
              val $resName: _root_.io.circe.Decoder.Result[$memberType] = ${decode(member)}

              if ($resName.isRight) {
                val $resultName: $memberType = ${extractFromRight(resName, memberType)}

                $acc
              } else ${castLeft(resName, tpe)}
            }
          """
        }

        def accumulatingDecode(member: Member): Tree = {
          val realFieldName =
            member.keyName.getOrElse(transformName(member.decodedName))
          if (globalUseDefaults && member.default.isDefined) {
            q"""if(c.downField($realFieldName).isInstanceOf[_root_.io.circe.FailedCursor]) {
              _root_.cats.data.Validated.Valid(${member.default.get})
              } else ${repr.decoder(member.tpe).name}.tryDecodeAccumulating(c.downField($realFieldName)
            )"""
          } else {
            q"""${repr.decoder(member.tpe).name}.tryDecodeAccumulating(c.downField($realFieldName)
            )"""
          }
        }

        val (results: List[Tree], resultNames: List[TermName]) =
          reversed.reverse.map {
            case (member, resultName) =>
              (
                q"""
              val $resultName: _root_.io.circe.Decoder.AccumulatingResult[${member.tpe}] =
                ${accumulatingDecode(member)}
            """,
                resultName
              )
          }.unzip

        val resultErrors: List[Tree] = resultNames.map { resultName =>
          q"errors($resultName)"
        }

        val resultAccumulating: Tree = q"""
          {
            ..$results

            val dfs: _root_.scala.List[_root_.io.circe.DecodingFailure] = List(..$resultErrors).flatten

            if (dfs.isEmpty) {
              _root_.cats.data.Validated.valid[
                _root_.cats.data.NonEmptyList[_root_.io.circe.DecodingFailure],
                $tpe
              ](${repr.instantiateAccumulating})
            } else {
              _root_.cats.data.Validated.invalid[
                _root_.cats.data.NonEmptyList[_root_.io.circe.DecodingFailure],
                $tpe
              ](_root_.cats.data.NonEmptyList.fromListUnsafe[_root_.io.circe.DecodingFailure](dfs))
            }
          }
        """

        c.Expr[Codec.AsObject[T]](
          q"""
          new _root_.io.circe.Codec.AsObject[$tpe] {
            ..$encoderInstanceDefs
            ..$decoderInstanceDefs

            final def encodeObject(a: $tpe): _root_.io.circe.JsonObject =
              _root_.io.circe.JsonObject.fromIterable($fields.flatten)

            final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = $result

            private[this] def errors(
              result: _root_.io.circe.Decoder.AccumulatingResult[_]
            ): _root_.scala.List[_root_.io.circe.DecodingFailure] = result match {
              case _root_.cats.data.Validated.Valid(_)   => _root_.scala.Nil
              case _root_.cats.data.Validated.Invalid(e) => e.toList
            }

            final override def decodeAccumulating(
              c: _root_.io.circe.HCursor
            ): _root_.io.circe.Decoder.AccumulatingResult[$tpe] = $resultAccumulating
          }
        """
        )
      }
    }
  }
}
