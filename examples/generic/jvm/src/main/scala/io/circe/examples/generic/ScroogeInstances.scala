package io.circe.examples.generic

import com.stripe.scrooge.shapes._
import io.circe.{ Decoder, ObjectEncoder }
import io.circe.examples.scrooge.{ BiggerStruct, SomethingStruct }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

object ScroogeInstances {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: ObjectEncoder[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct1: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct1: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct1: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct1: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct2: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct2: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct2: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct2: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct3: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct3: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct3: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct3: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct4: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct4: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct4: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct4: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct5: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct5: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct5: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct5: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct6: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct6: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct6: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct6: ObjectEncoder[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct7: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct7: ObjectEncoder[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct7: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct7: ObjectEncoder[BiggerStruct] = deriveEncoder
}
