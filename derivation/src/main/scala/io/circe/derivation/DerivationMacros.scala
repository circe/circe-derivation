package io.circe.derivation

import io.circe.{ Decoder, Encoder, ObjectEncoder }
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) extends ScalaVersionCompat {
  import c.universe._

  private[this] val encoderSymbol: Symbol = c.symbolOf[Encoder.type]
  private[this] val decoderSymbol: Symbol = c.symbolOf[Decoder.type]
  private[this] val encoderTC: Type = typeOf[Encoder[_]].typeConstructor
  private[this] val decoderTC: Type = typeOf[Decoder[_]].typeConstructor

  private[this] case class Instance(tc: Type, tpe: Type, name: TermName) {
    def resolve(): Tree = c.inferImplicitValue(appliedType(tc, List(tpe))) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"Could not find implicit $tpe")
      case instance => instance
    }
  }

  private[this] case class Instances(encoder: Instance, decoder: Instance)
  private[this] case class Member(name: TermName, decodedName: String, tpe: Type)

  private[this] case class ProductRepr(members: List[Member]) {
    val instances: List[Instances] =
      members.foldLeft(List.empty[Instances]) {
        case (acc, Member(_, _, tpe)) if acc.find(_.encoder.tpe =:= tpe).isEmpty =>
          val instances = Instances(
            Instance(encoderTC, tpe, TermName(c.freshName("encoder"))),
            Instance(decoderTC, tpe, TermName(c.freshName("decoder")))
          )

          instances :: acc
        case (acc, _) => acc
      }.reverse

    private[this] def fail(tpe: Type): Nothing = c.abort(c.enclosingPosition, s"Invalid instance lookup for $tpe")

    def encoder(tpe: Type): Instance = instances.map(_.encoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
    def decoder(tpe: Type): Instance = instances.map(_.decoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
  }

  private[this] def membersFromPrimaryConstr(tpe: Type): Option[List[Member]] = tpe.decls.collectFirst {
    case m: MethodSymbol if m.isPrimaryConstructor => m.paramLists.flatten.map { field =>
      val asf = tpe.decl(field.name).asMethod.returnType.asSeenFrom(tpe, tpe.typeSymbol)

      Member(field.name.toTermName, field.name.decodedName.toString, asf)
    }
  }

  private[this] def productRepr(tpe: Type): Option[ProductRepr] =
    membersFromPrimaryConstr(tpe).map(ProductRepr(_))

  private[this] def fail(tpe: Type): Nothing = c.abort(
    c.enclosingPosition,
    s"Could not identify primary constructor for $tpe"
  )

  private[this] def checkValSafety(owner: Symbol)(tree: Tree): Boolean = tree match {
    case q"$f(...$x)" if x.nonEmpty => checkValSafety(owner)(f) && x.forall(_.forall(checkValSafety(owner)))
    case x if x.isTerm => x.symbol.owner == owner
    case x => false
  }

  private[this] def checkEncoderValSafety(tree: Tree): Boolean = checkValSafety(encoderSymbol)(tree)
  private[this] def checkDecoderValSafety(tree: Tree): Boolean = checkValSafety(decoderSymbol)(tree)

  private[this] val resName: TermName = TermName("res")

  private[this] def extractFromRight(value: c.TermName, tpe: c.Type): c.Tree = {
    import c.universe._

    q"$value.asInstanceOf[_root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe]].${ rightValueName(c) }"
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
  def materializeEncoder[T: c.WeakTypeTag]: c.Expr[ObjectEncoder[T]] = materializeEncoderImpl[T](None)

  def materializeDecoderWithNameTransformation[T: c.WeakTypeTag](
    nameTransformation: c.Expr[String => String]
  ): c.Expr[Decoder[T]] = materializeDecoderImpl[T](Some(nameTransformation))

  def materializeEncoderWithNameTransformation[T: c.WeakTypeTag](
    nameTransformation: c.Expr[String => String]
  ): c.Expr[ObjectEncoder[T]] = materializeEncoderImpl[T](Some(nameTransformation))

  private[this] def materializeDecoderImpl[T: c.WeakTypeTag](
    nameTransformation: Option[c.Expr[String => String]]
  ): c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    def transformName(name: String): Tree = nameTransformation.fold[Tree](q"$name")(f => q"$f($name)")

    productRepr(tpe).fold(fail(tpe)) { repr =>
      if (repr.members.isEmpty) {
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

        val membersWithNames = repr.members.zipWithIndex.map {
          case (member, i) => (member, TermName(s"res$i"))
        }

        val reversed = membersWithNames.reverse

        def decode(member: Member): Tree =
          q"c.get[${ member.tpe }](${ transformName(member.decodedName) })(${ repr.decoder(member.tpe).name })"

        val last: Tree = q"""
          {
            val $resName: _root_.io.circe.Decoder.Result[${ reversed.head._1.tpe }] =
              ${ decode(reversed.head._1) }

            if ($resName.isRight) {
              val ${ reversed.head._2 }: ${ reversed.head._1.tpe } =
                ${ extractFromRight(resName, reversed.head._1.tpe) }

              new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](
                new $tpe(..${ membersWithNames.map(_._2) })
              ): _root_.io.circe.Decoder.Result[$tpe]
            } else ${ castLeft(resName, tpe) }
          }
        """

        val result: Tree = reversed.tail.foldLeft(last) {
          case (acc, (member @ Member(_, _, memberType), resultName)) => q"""
            {
              val $resName: _root_.io.circe.Decoder.Result[$memberType] = ${ decode(member) }

              if ($resName.isRight) {
                val $resultName: $memberType = ${ extractFromRight(resName, memberType) }

                $acc
              } else ${ castLeft(resName, tpe) }
            }
          """
        }

        val results: List[Tree] = reversed.reverse.map {
          case (member, resultName) => q"""
            val $resultName: _root_.io.circe.AccumulatingDecoder.Result[${ member.tpe }] =
              ${ repr.decoder(member.tpe).name }.tryDecodeAccumulating(
                c.downField(${ transformName(member.decodedName) })
              )
          """
        }

        val resultNames: List[TermName] = membersWithNames.map(_._2)

        val resultAccumulating: Tree = q"""
          {
            ..$results

            val dfs: _root_.scala.List[_root_.io.circe.DecodingFailure] = errors($resultNames)

            if (dfs.isEmpty) {
              _root_.cats.data.Validated.valid[
                _root_.cats.data.NonEmptyList[_root_.io.circe.DecodingFailure],
                $tpe
              ](new $tpe(..${ membersWithNames.map(mn => extractFromValid(mn._2, mn._1.tpe)) }))
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
                results: _root_.scala.List[_root_.io.circe.AccumulatingDecoder.Result[_]]
              ): _root_.scala.List[_root_.io.circe.DecodingFailure] = results.flatMap {
                case _root_.cats.data.Validated.Valid(_) => _root_.scala.Nil
                case _root_.cats.data.Validated.Invalid(errors) => errors.toList
              }

              final override def decodeAccumulating(
                c: _root_.io.circe.HCursor
              ): _root_.io.circe.AccumulatingDecoder.Result[$tpe] = $resultAccumulating
            }
          """
        )
      }
    }
  }

  private[this] def materializeEncoderImpl[T: c.WeakTypeTag](
    nameTransformation: Option[c.Expr[String => String]]
  ): c.Expr[ObjectEncoder[T]] = {
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

      val fields: List[Tree] = repr.members.map {
        case Member(name, decodedName, tpe) =>
          repr.encoder(tpe) match {
            case Instance(_, _, instanceName) => q"""
              _root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                ${ transformName(decodedName) },
                $instanceName.apply(a.$name)
              )
            """
          }
      }

      c.Expr[ObjectEncoder[T]](
        q"""
          new _root_.io.circe.ObjectEncoder[$tpe] {
            ..$instanceDefs

            final def encodeObject(a: $tpe): _root_.io.circe.JsonObject =
              _root_.io.circe.JsonObject.fromIterable($fields)
          }
        """
      )
    }
  }
}
