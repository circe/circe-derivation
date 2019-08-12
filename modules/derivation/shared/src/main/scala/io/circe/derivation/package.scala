package io.circe

import scala.language.experimental.macros

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoder[A]
  final def deriveEncoder[A]: Encoder.AsObject[A] = macro DerivationMacros.materializeEncoder[A]
  final def deriveCodec[A]: Codec.AsObject[A] = macro DerivationMacros.materializeCodec[A]

  final def deriveDecoder[A](
    nameTransformation: String => String,
    useDefaults: Boolean,
    discriminator: Discriminator
  ): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithNameTransformation[A]

  final def deriveEncoder[A](
    nameTransformation: String => String,
    discriminator: Discriminator
  ): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithNameTransformation[A]

  final def deriveCodec[A](
    nameTransformation: String => String,
    useDefaults: Boolean,
    discriminator: Discriminator
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithNameTransformation[A]
}
