package io.circe

import scala.language.experimental.macros

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoder[A]
  final def deriveEncoder[A]: Encoder.AsObject[A] = macro DerivationMacros.materializeEncoder[A]
  final def deriveCodec[A]: Codec.AsObject[A] = macro DerivationMacros.materializeCodec[A]

  final def deriveDecoder[A](
    transformMemberNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithTransformMemberNames[A]

  final def deriveEncoder[A](
    transformMemberNames: String => String,
    discriminator: Option[String]
  ): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithTransformMemberNames[A]

  final def deriveCodec[A](
    transformMemberNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithTransformMemberNames[A]
}
