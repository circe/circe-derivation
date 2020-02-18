package io.circe.derivation

import io.circe.{ Decoder, Encoder }
import io.circe.examples.scrooge._
import io.circe.generic.semiauto.{ deriveDecoder => genericDeriveDecoder, deriveEncoder => genericDeriveEncoder }

object ScroogeGenericAutoCodecs {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = genericDeriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = genericDeriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = genericDeriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = genericDeriveEncoder
}
