package io.circe.derivation

import io.circe.{ Decoder, ObjectEncoder }
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) {
  import c.universe._

  private[this] case class Instance(tpe: Type, name: TermName, definition: Tree)
  private[this] case class Instances(encoder: Instance, decoder: Instance)
  private[this] case class Member(name: TermName, decodedName: String, tpe: Type)

  private[this] case class ProductRepr(members: List[Member]) {
    val instances: List[Instances] =
      members.foldLeft(List.empty[Instances]) {
        case (acc, Member(_, _, tpe)) if acc.find(_.encoder.tpe =:= tpe).isEmpty =>
          val instances = Instances(
            Instance(tpe, TermName(c.freshName("encoder")), q"_root_.io.circe.Encoder[$tpe]"),
            Instance(tpe, TermName(c.freshName("decoder")), q"_root_.io.circe.Decoder[$tpe]")
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
      Member(field.name.toTermName, field.name.decodedName.toString, tpe.decl(field.name).asMethod.returnType)
    }
  }

  private[this] def productRepr(tpe: Type): Option[ProductRepr] =
    membersFromPrimaryConstr(tpe).map(ProductRepr(_))

  private[this] def fail(tpe: Type): Nothing = c.abort(
    c.enclosingPosition,
    s"Could not identify primary constructor for $tpe"
  )

  private[this] val resName: TermName = TermName("res")

  private[this] def extractFromRight(value: TermName, tpe: Type): Tree =
    q"$value.asInstanceOf[_root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe]].value"

  private[this] def castLeft(value: TermName, tpe: Type): Tree =
    q"$value.asInstanceOf[_root_.io.circe.Decoder.Result[$tpe]]"

  def materializeDecoderImpl[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    productRepr(tpe).fold(fail(tpe)) { repr =>
      if (repr.members.isEmpty) {
        c.Expr[Decoder[T]](q"_root_.io.circe.Decoder.const(new $tpe())")
      } else {
        val instanceDefs: List[Tree] = repr.instances.map(_.decoder).map {
          case Instance(instanceType, name, definition) =>
            q"private[this] val $name: _root_.io.circe.Decoder[$instanceType] = $definition"
        }

        val reversed = repr.members.zipWithIndex.reverse.map {
          case (member, i) => (member, TermName(s"res$i"))
        }

        def decode(member: Member): Tree =
          q"c.get[${ member.tpe }](${ member.decodedName })(${ repr.decoder(member.tpe).name })"

        val last: Tree = q"""
          {
            val $resName: _root_.io.circe.Decoder.Result[${ reversed.head._1.tpe }] =
              ${ decode(reversed.head._1) }

            if ($resName.isRight) {
              val ${ reversed.head._2 }: ${ reversed.head._1.tpe } =
                ${ extractFromRight(resName, reversed.head._1.tpe) }

              new _root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe](
                new $tpe(..${ reversed.map(_._2).reverse })
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

        c.Expr[Decoder[T]](
          q"""
            new _root_.io.circe.Decoder[$tpe] {
              ..$instanceDefs

              final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$tpe] = $result
            }
          """
        )
      }
    }
  }


  def materializeEncoderImpl[T: c.WeakTypeTag]: c.Expr[ObjectEncoder[T]] = {
    val tpe = weakTypeOf[T]

    productRepr(tpe).fold(fail(tpe)) { repr =>
      val instanceDefs: List[Tree] = repr.instances.map(_.encoder).map {
        case Instance(instanceType, name, definition) =>
          q"private[this] val $name: _root_.io.circe.Encoder[$instanceType] = $definition"
      }

      val fields: List[Tree] = repr.members.map {
        case Member(name, decodedName, tpe) =>
          repr.encoder(tpe) match {
            case Instance(instanceType, instance, _) => q"""
              _root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.io.circe.Json](
                $decodedName,
                $instance.apply(a.$name)
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
