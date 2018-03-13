package io.circe.examples.derivation

import io.circe.{ Decoder, ObjectEncoder }
import io.circe.derivation.{ deriveDecoder, deriveEncoder }
import io.circe.examples.scrooge.{ BiggerStruct, SomethingStruct }

object ScroogeInstances {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: ObjectEncoder[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: ObjectEncoder[BiggerStruct] = deriveEncoder
}
