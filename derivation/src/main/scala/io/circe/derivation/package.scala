package io.circe

import scala.language.experimental.macros

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoderImpl[A]
  final def deriveEncoder[A]: ObjectEncoder[A] = macro DerivationMacros.materializeEncoderImpl[A]
}
