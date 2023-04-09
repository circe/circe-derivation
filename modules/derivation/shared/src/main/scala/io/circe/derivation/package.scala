package io.circe

import scala.language.experimental.macros

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoder[A]
  final def deriveEncoder[A]: Encoder.AsObject[A] = macro DerivationMacros.materializeEncoder[A]
  final def deriveCodec[A]: Codec.AsObject[A] = macro DerivationMacros.materializeCodec[A]

  final def deriveDecoder[A](
    transformNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String],
    strictDecoding: Boolean
  ): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithTransformNames[A]

  final def deriveDecoder[A](transformNames: String => String): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithTransformNamesAndDefaults[A]

  final def deriveEncoder[A](
    transformNames: String => String,
    discriminator: Option[String]
  ): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithTransformNames[A]

  final def deriveEncoder[A](transformNames: String => String): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithTransformNamesAndDefaults[A]

  final def deriveCodec[A](
    transformNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String],
    strictDecoding: Boolean
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithTransformNames[A]

  final def deriveCodec[A](
    transformNames: String => String
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithTransformNamesAndDefaults[A]
}
