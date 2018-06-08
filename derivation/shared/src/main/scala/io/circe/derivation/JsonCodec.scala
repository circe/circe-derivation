package io.circe.derivation

import io.circe.{derivation, Decoder, Encoder, ObjectEncoder}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class JsonCodec(
  encodeOnly: Boolean = false,
  decodeOnly: Boolean = false
) extends scala.annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro GenericJsonCodecMacros.jsonCodecAnnotationMacro
}

private[derivation] final class GenericJsonCodecMacros(val c: blackbox.Context) {
  import c.universe._

  def jsonCodecAnnotationMacro(annottees: Tree*): Tree = constructJsonCodec(annottees: _*)

  private[this] def isCaseClassOrSealed(clsDef: ClassDef) =
    clsDef.mods.hasFlag(Flag.CASE) || clsDef.mods.hasFlag(Flag.SEALED)

  private[this] final def constructJsonCodec(annottees: Tree*): Tree = annottees match {
    case List(clsDef: ClassDef) if isCaseClassOrSealed(clsDef) =>
      q"""
       $clsDef
       object ${ clsDef.name.toTermName } {
         ..${ codec(clsDef) }
       }
       """
    case List(
    clsDef: ClassDef,
    q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
    ) if isCaseClassOrSealed(clsDef) =>
      q"""
       $clsDef
       object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf =>
         ..$objDefs
         ..${ codec(clsDef) }
       }
       """
    case _ => c.abort(c.enclosingPosition,
      "Invalid annotation target: must be a case class or a sealed trait/class")
  }

  private[this] val DecoderClass = typeOf[Decoder[_]].typeSymbol.asType
  private[this] val EncoderClass = typeOf[Encoder[_]].typeSymbol.asType
  private[this] val ObjectEncoderClass = typeOf[ObjectEncoder[_]].typeSymbol.asType

  private[this] val macroName: Tree = {
    c.prefix.tree match {
      case Apply(Select(New(name), _), _) => name
      case _ => c.abort(c.enclosingPosition, "Unexpected macro application")
    }
  }

  private[this] val codecType: JsonCodecType = {
    c.prefix.tree match {
      case q"new ${`macroName`}()" => JsonCodecType.Both
      case q"new ${`macroName`}(encodeOnly = true)" => JsonCodecType.EncodeOnly
      case q"new ${`macroName`}(decodeOnly = true)" => JsonCodecType.DecodeOnly
      case _ => c.abort(c.enclosingPosition, s"Unsupported arguments supplied to @$macroName")
    }
  }

  private[this] def codec(clsDef: ClassDef): List[Tree] = {
    val tpname = clsDef.name
    val tparams = clsDef.tparams
    val decodeNme = TermName("decode" + tpname.decodedName)
    val encodeNme = TermName("encode" + tpname.decodedName)
    val (decoder, encoder) = if (tparams.isEmpty) {
      val Type = tpname
      (
        q"""implicit val $decodeNme: $DecoderClass[$Type] = _root_.io.circe.derivation.deriveDecoder[$Type]""",
        q"""implicit val $encodeNme: $ObjectEncoderClass[$Type] = _root_.io.circe.derivation.deriveEncoder[$Type]"""
      )
    } else {
      val tparamNames = tparams.map(_.name)
      def mkImplicitParams(typeSymbol: TypeSymbol) =
        tparamNames.zipWithIndex.map {
          case (tparamName, i) =>
            val paramName = TermName(s"instance$i")
            val paramType = tq"$typeSymbol[$tparamName]"
            q"$paramName: $paramType"
        }
      val decodeParams = mkImplicitParams(DecoderClass)
      val encodeParams = mkImplicitParams(EncoderClass)
      val Type = tq"$tpname[..$tparamNames]"
      (
        q"""implicit def $decodeNme[..$tparams](implicit ..$decodeParams): $DecoderClass[$Type] =
           _root_.io.circe.derivation.deriveDecoder[$Type]""",
        q"""implicit def $encodeNme[..$tparams](implicit ..$encodeParams): $ObjectEncoderClass[$Type] =
           _root_.io.circe.derivation.deriveEncoder[$Type]"""
      )
    }
    codecType match {
      case JsonCodecType.Both => List(decoder, encoder)
      case JsonCodecType.DecodeOnly => List(decoder)
      case JsonCodecType.EncodeOnly => List(encoder)
    }
  }
}

private sealed trait JsonCodecType
private object JsonCodecType {
  case object Both extends JsonCodecType
  case object DecodeOnly extends JsonCodecType
  case object EncodeOnly extends JsonCodecType
}
