package io.circe

import scala.language.experimental.macros

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoder[A]
  final def deriveEncoder[A]: Encoder.AsObject[A] = macro DerivationMacros.materializeEncoder[A]

  final def deriveDecoder[A](nameTransformation: String => String): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithNameTransformation[A]
  final def deriveEncoder[A](nameTransformation: String => String): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithNameTransformation[A]
}
