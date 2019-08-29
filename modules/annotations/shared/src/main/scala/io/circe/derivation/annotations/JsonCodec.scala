package io.circe.derivation.annotations

import io.circe.{ Codec, Decoder, Encoder }
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class JsonCodec(
  config: Configuration = Configuration.default
) extends scala.annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro GenericJsonCodecMacros.jsonCodecAnnotationMacro
}

class SnakeCaseJsonCodec extends scala.annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro GenericJsonCodecMacros.jsonCodecAnnotationMacro
}

class KebabCaseJsonCodec extends scala.annotation.StaticAnnotation {
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
       object ${clsDef.name.toTermName} {
         ${codec(clsDef)}
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
         ${codec(clsDef)}
       }
       """
    case _ => c.abort(c.enclosingPosition, "Invalid annotation target: must be a case class or a sealed trait/class")
  }

  private[this] val DecoderClass = typeOf[Decoder[_]].typeSymbol.asType
  private[this] val EncoderClass = typeOf[Encoder[_]].typeSymbol.asType
  private[this] val AsObjectEncoderClass = typeOf[Encoder.AsObject[_]].typeSymbol.asType
  private[this] val AsObjectCodecClass = typeOf[Codec.AsObject[_]].typeSymbol.asType

  private[this] val macroName: Tree = {
    c.prefix.tree match {
      case Apply(Select(New(name), _), _) => name
      case _                              => c.abort(c.enclosingPosition, "Unexpected macro application")
    }
  }

  private[this] val defaultCfg: Tree =
    q"_root_.io.circe.derivation.annotations.Configuration.default"

  private[this] val snakeCaseMemberNamesCfg: Tree =
    q"_root_.io.circe.derivation.annotations.Configuration.default.withSnakeCaseMemberNames"

  private[this] val kebabCaseMemberNamesCfg: Tree =
    q"_root_.io.circe.derivation.annotations.Configuration.default.withKebabCaseMemberNames"

  private val snakeCaseAnnotationName = TypeName("SnakeCaseJsonCodec")

  private[this] val (codecType: JsonCodecType, config: Tree) = {
    macroName match {
      case Ident(TypeName("SnakeCaseJsonCodec")) => (JsonCodecType.SnakeCaseJsonCodec, snakeCaseMemberNamesCfg)
      case Ident(TypeName("KebabCaseJsonCodec")) => (JsonCodecType.KebabCaseJsonCodec, kebabCaseMemberNamesCfg)
      case _ =>
        c.prefix.tree match {
          case q"new ${`macroName` }()"              => (JsonCodecType.Both, defaultCfg)
          case q"new ${`macroName` }(config = $cfg)" => (codecFrom(c.typecheck(cfg)), cfg)
          case q"new ${`macroName` }($cfg)"          => (codecFrom(c.typecheck(cfg)), cfg)
          case _                                     => c.abort(c.enclosingPosition, s"Unsupported arguments supplied to @$macroName")
        }
    }
  }

  private[this] def codecFrom(tree: Tree): JsonCodecType =
    tree.tpe.dealias match {
      case t if t <:< typeOf[Configuration.Codec] =>
        JsonCodecType.Both
      case t if t <:< typeOf[Configuration.DecodeOnly] =>
        JsonCodecType.DecodeOnly
      case t if t <:< typeOf[Configuration.EncodeOnly] =>
        JsonCodecType.EncodeOnly
      case t =>
        c.warning(
          c.enclosingPosition,
          "Couldn't determine type of configuration; will produce both encoder and decoder"
        )
        JsonCodecType.Both
    }

  private[this] val cfgTransformMemberNames =
    q"$config.transformMemberNames"
  private[this] val cfgUseDefaults =
    q"$config.useDefaults"
  private[this] val cfgDiscriminator =
    q"$config.discriminator"

  private[this] val defaultDiscriminator: Tree =
    q"_root_.io.circe.derivation.Discriminator.default"

  private[this] def codec(clsDef: ClassDef): Tree = {
    val tpname = clsDef.name
    val tparams = clsDef.tparams
    val decoderName = TermName("decode" + tpname.decodedName)
    val encoderName = TermName("encode" + tpname.decodedName)
    val codecName = TermName("codecFor" + tpname.decodedName)
    val (decoder, encoder, codec) = if (tparams.isEmpty) {
      val Type = tpname
      (
        q"""implicit val $decoderName: $DecoderClass[$Type] =
            _root_.io.circe.derivation.deriveDecoder[$Type]($cfgTransformMemberNames, $cfgUseDefaults, $cfgDiscriminator)""",
        q"""implicit val $encoderName: $AsObjectEncoderClass[$Type] =
            _root_.io.circe.derivation.deriveEncoder[$Type]($cfgTransformMemberNames, $cfgDiscriminator)""",
        q"""implicit val $codecName: $AsObjectCodecClass[$Type] =
            _root_.io.circe.derivation.deriveCodec[$Type]($cfgTransformMemberNames, $cfgUseDefaults, $cfgDiscriminator)"""
      )
    } else {
      val tparamNames = tparams.map(_.name)
      def mkImplicitParams(prefix: String, typeSymbol: TypeSymbol) =
        tparamNames.zipWithIndex.map {
          case (tparamName, i) =>
            val paramName = TermName(s"$prefix$i")
            val paramType = tq"$typeSymbol[$tparamName]"
            q"$paramName: $paramType"
        }
      val decodeParams = mkImplicitParams("decode", DecoderClass)
      val encodeParams = mkImplicitParams("encode", EncoderClass)
      val Type = tq"$tpname[..$tparamNames]"
      (
        q"""implicit def $decoderName[..$tparams](implicit ..$decodeParams): $DecoderClass[$Type] =
           _root_.io.circe.derivation.deriveDecoder[$Type]($cfgTransformMemberNames, $cfgUseDefaults, $cfgDiscriminator)""",
        q"""implicit def $encoderName[..$tparams](implicit ..$encodeParams): $AsObjectEncoderClass[$Type] =
           _root_.io.circe.derivation.deriveEncoder[$Type]($cfgTransformMemberNames, $cfgDiscriminator)""",
        q"""implicit def $codecName[..$tparams](implicit
            ..${decodeParams ++ encodeParams}
          ): $AsObjectCodecClass[$Type] =
            _root_.io.circe.derivation.deriveCodec[$Type]($cfgTransformMemberNames, $cfgUseDefaults, $cfgDiscriminator)"""
      )
    }
    codecType match {
      case JsonCodecType.Both               => codec
      case JsonCodecType.SnakeCaseJsonCodec => codec
      case JsonCodecType.KebabCaseJsonCodec => codec
      case JsonCodecType.DecodeOnly         => decoder
      case JsonCodecType.EncodeOnly         => encoder
    }
  }
}

private sealed trait JsonCodecType
private object JsonCodecType {
  case object Both extends JsonCodecType
  case object DecodeOnly extends JsonCodecType
  case object EncodeOnly extends JsonCodecType
  case object SnakeCaseJsonCodec extends JsonCodecType
  case object KebabCaseJsonCodec extends JsonCodecType
}
