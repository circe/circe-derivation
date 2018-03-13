package io.circe.examples.generic

import com.stripe.scrooge.shapes._
import io.circe.{ Decoder, ObjectEncoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.examples.scrooge.{ BiggerStruct, SomethingStruct }

object ScroogeInstances {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: ObjectEncoder[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: ObjectEncoder[BiggerStruct] = deriveEncoder
}
