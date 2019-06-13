package io.circe.derivation

import io.circe.{ Decoder, Encoder }
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) extends ScalaVersionCompat {
  import c.universe._

  private[this] val encoderSymbol: Symbol = c.symbolOf[Encoder.type]
  private[this] val decoderSymbol: Symbol = c.symbolOf[Decoder.type]
  private[this] val encoderTC: Type = typeOf[Encoder[_]].typeConstructor
  private[this] val decoderTC: Type = typeOf[Decoder[_]].typeConstructor

  private[this] def failWithMessage(message: String): Nothing = c.abort(c.enclosingPosition, message)

  private[this] case class Instance(tc: Type, tpe: Type, name: TermName) {
    def resolve(): Tree = c.inferImplicitValue(appliedType(tc, List(tpe))) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"Could not find $tc instance for $tpe")
      case instance  => instance
    }
  }

  private[this] case class Instances(tpe: Type, encoder: Instance, decoder: Instance)
  private[this] case class Member(name: TermName, decodedName: String, tpe: Type)

  private[this] object Member {
    final def fromSymbol(tpe: Type)(sym: Symbol): Member = {
      val memberName = sym.name
      val memberDecl = tpe.decl(memberName)

      if (!memberDecl.isMethod)
        failWithMessage(
          s"No method $memberName in $tpe (this is probably because a constructor parameter isn't a val)"
        )

      Member(
        memberName.toTermName,
        memberName.decodedName.toString,
        memberDecl.asMethod.returnType.asSeenFrom(tpe, tpe.typeSymbol)
      )
    }
  }

  private[this] sealed abstract class ProductRepr {
    protected[this] def instantiate(params: List[List[Tree]]): Tree

    def instantiate: Tree = instantiate(paramListsWithNames.map(_.map(p => q"${p._2}")))
    def instantiateAccumulating: Tree = instantiate(
      paramListsWithNames.map(
        _.map {
          case (Member(_, _, tpe), name) => extractFromValid(name, tpe)
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
          case (acc, (Member(_, _, tpe), i)) if acc.find(_.tpe =:= tpe).isEmpty =>
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
      s.alternatives.collect {
        case m: MethodSymbol => m.paramLists
      }.sortBy(-_.map(_.size).sum).headOption.map { applyParams =>
        ProductReprWithApply(tpe, applyParams.map(_.map(Member.fromSymbol(tpe))))
      }
  }

  private[this] def membersFromPrimaryConstr(tpe: Type): Option[ProductRepr] =
    if (tpe.typeSymbol.isAbstract) {
      None
    } else {
      tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor && m.isPublic && !m.isAbstract =>
          ProductReprWithConstr(tpe, m.paramLists.map(_.map(Member.fromSymbol(tpe))))
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

  def materializeDecoder[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = materializeDecoderImpl[T](None)
  def materializeEncoder[T: c.WeakTypeTag]: c.Expr[Encoder.AsObject[T]] = materializeEncoderImpl[T](None)

  def materializeDecoderWithNameTransformation[T: c.WeakTypeTag](
    nameTransformation: c.Expr[String => String]
  ): c.Expr[Decoder[T]] = materializeDecoderImpl[T](Some(nameTransformation))

  def materializeEncoderWithNameTransformation[T: c.WeakTypeTag](
    nameTransformation: c.Expr[String => String]
  ): c.Expr[Encoder.AsObject[T]] = materializeEncoderImpl[T](Some(nameTransformation))

  private[this] def materializeDecoderImpl[T: c.WeakTypeTag](
    nameTransformation: Option[c.Expr[String => String]]
  ): c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    def transformName(name: String): Tree = nameTransformation.fold[Tree](q"$name")(f => q"$f($name)")

    productRepr(tpe).fold(fail(tpe)) { repr =>
      if (repr.paramLists.flatten.isEmpty) {
        c.Expr[Decoder[T]](q"_root_.io.circe.Decoder.const(new $tpe())")
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

        def decode(member: Member): Tree =
          q"this.${repr.decoder(member.tpe).name}.tryDecode(c.downField(${transformName(member.decodedName)}))"

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
          case (acc, (member @ Member(_, _, memberType), resultName)) => q"""
            {
              val $resName: _root_.io.circe.Decoder.Result[$memberType] = ${decode(member)}

              if ($resName.isRight) {
                val $resultName: $memberType = ${extractFromRight(resName, memberType)}

                $acc
              } else ${castLeft(resName, tpe)}
            }
          """
        }

        val (results: List[Tree], resultNames: List[TermName]) = reversed.reverse.map {
          case (member, resultName) =>
            (
              q"""
              val $resultName: _root_.io.circe.Decoder.AccumulatingResult[${member.tpe}] =
                ${repr.decoder(member.tpe).name}.tryDecodeAccumulating(
                  c.downField(${transformName(member.decodedName)})
                )
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

  private[this] def materializeEncoderImpl[T: c.WeakTypeTag](
    nameTransformation: Option[c.Expr[String => String]]
  ): c.Expr[Encoder.AsObject[T]] = {
    val tpe = weakTypeOf[T]

    def transformName(name: String): Tree = nameTransformation.fold[Tree](q"$name")(f => q"$f($name)")

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

      val fields: List[Tree] = repr.paramLists.flatten.map {
        case Member(name, decodedName, tpe) =>
          repr.encoder(tpe) match {
            case Instance(_, _, instanceName) => q"""
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
