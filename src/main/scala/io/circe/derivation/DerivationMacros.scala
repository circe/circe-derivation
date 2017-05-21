package io.circe.derivation

import io.circe.{ Decoder, ObjectEncoder }
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) {
  import c.universe._

  private[this] case class Member(name: Name, decodedName: String, tpe: Type)

  private[this] def fail(tpe: Type): Nothing = c.abort(
    c.enclosingPosition,
    s"Could not identify primary constructor for $tpe"
  )

  private[this] def membersFromPrimaryConstr(tpe: Type): Option[List[Member]] = tpe.decls.collectFirst {
    case m: MethodSymbol if m.isPrimaryConstructor => m.paramLists.flatten.map { field =>
      Member(field.name, field.name.decodedName.toString, tpe.decl(field.name).typeSignature)
    }
  }

  def materializeDecoderImpl[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    membersFromPrimaryConstr(tpe).fold(fail(tpe)) { members =>
      val fieldCount = members.size
      val functionParameters = members.map {
        case Member(fieldName, _, fieldType) =>
          val termName = TermName(fieldName.toString)
          q"$termName: $fieldType"
      }

      val parameters = members.map {
        case Member(fieldName, _, _) =>
          val termName = TermName(fieldName.toString)
          q"$termName"
      }

      val methodName = TermName(s"forProduct$fieldCount")

      if (fieldCount > 0) c.Expr[Decoder[T]](
        q"""
          _root_.io.circe.Decoder.$methodName[..${ members.map(_.tpe) }, $tpe](..${ members.map(_.decodedName) })(
            (..$functionParameters) => new $tpe(..$parameters)
          )
        """
      ) else c.Expr[Decoder[T]](q"_root_.io.circe.Decoder.const(new $tpe())")
    }
  }

  def materializeEncoderImpl[T: c.WeakTypeTag]: c.Expr[ObjectEncoder[T]] = {
    val tpe = weakTypeOf[T]

    membersFromPrimaryConstr(tpe).fold(fail(tpe)) { members =>
      val fieldCount = members.size
      val invocations = members.map {
        case Member(fieldName, _, _) =>
          val termName = TermName(fieldName.toString)
          q"toEncode.$termName"
      }

      val methodName = TermName(s"forProduct$fieldCount")

      if (fieldCount > 0) c.Expr[ObjectEncoder[T]](
        q"""
          _root_.io.circe.Encoder.$methodName[..${ members.map(_.tpe) }, $tpe](..${ members.map(_.decodedName) })(
            toEncode => (..$invocations)
          )
        """
      ) else c.Expr[ObjectEncoder[T]](
        q"""
          _root_.io.circe.ObjectEncoder.instance[$tpe](_ => _root_.io.circe.JsonObject.empty)
        """
      )
    }
  }
}
