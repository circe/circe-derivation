package io.circe.derivation

import io.circe.{ Decoder, ObjectEncoder }
import io.circe.examples.scrooge._
import io.circe.generic.semiauto.{
  deriveDecoder => genericDeriveDecoder,
  deriveEncoder => genericDeriveEncoder
}
import com.stripe.scrooge.shapes._

object ScroogeGenericAutoCodecs {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = genericDeriveDecoder
  implicit val encodeSomethingStruct: ObjectEncoder[SomethingStruct] = genericDeriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = genericDeriveDecoder
  implicit val encodeBiggerStruct: ObjectEncoder[BiggerStruct] = genericDeriveEncoder
}
