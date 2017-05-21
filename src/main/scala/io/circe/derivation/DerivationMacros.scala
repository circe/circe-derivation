package io.circe.derivation

import io.circe.{ Decoder, ObjectEncoder }
import scala.reflect.macros.blackbox

class DerivationMacros(val c: blackbox.Context) {
  import c.universe._

  def fail(tpe: Type): Nothing = c.abort(c.enclosingPosition, s"Could not identify primary constructor for $tpe")

  def materializeDecoderImpl[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    val primaryConstructor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }

    primaryConstructor.fold(fail(tpe)) { constructor =>
      val fieldNames: List[Name] = constructor.paramLists.flatten.map(_.name)
      val decodedNames: List[String] = fieldNames.map(_.decodedName.toString)
      val fieldTypes: List[Type] = constructor.paramLists.flatten.map { field =>
        tpe.decl(field.name).typeSignature
      }
      val fieldCount = fieldNames.size
      val functionParameters = fieldNames.zip(fieldTypes).map {
        case (fieldName, fieldType) =>
          val termName = TermName(fieldName.toString)
          q"$termName: $fieldType"
      }
      val parameters = fieldNames.map { fieldName =>
        val termName = TermName(fieldName.toString)
        q"$termName"
      }

      val methodName = TermName(s"forProduct$fieldCount")

      if (fieldCount > 0) c.Expr[Decoder[T]](
        q"""
          _root_.io.circe.Decoder.$methodName[..$fieldTypes, $tpe](..$decodedNames)(
            (..$functionParameters) => new $tpe(..$parameters)
          )
        """
      ) else c.Expr[Decoder[T]](q"_root_.io.circe.Decoder.const(new $tpe())")
    }
  }

  def materializeEncoderImpl[T: c.WeakTypeTag]: c.Expr[ObjectEncoder[T]] = {
    val tpe = weakTypeOf[T]

    val primaryConstructor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }

    primaryConstructor.fold(fail(tpe)) { constructor =>
      val fieldNames: List[Name] = constructor.paramLists.flatten.map(_.name)
      val decodedNames: List[String] = fieldNames.map(_.decodedName.toString)
      val fieldTypes: List[Type] = constructor.paramLists.flatten.map { field =>
        tpe.decl(field.name).typeSignature
      }
      val fieldCount = fieldNames.size
      val invocations = fieldNames.map { fieldName => 
        val termName = TermName(fieldName.toString)
        q"toEncode.$termName"
      }

      val methodName = TermName(s"forProduct$fieldCount")

      if (fieldCount > 0) c.Expr[ObjectEncoder[T]](
        q"""
          _root_.io.circe.Encoder.$methodName[..$fieldTypes, $tpe](..$decodedNames)(
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
