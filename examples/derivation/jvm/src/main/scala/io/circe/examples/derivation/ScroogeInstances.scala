package io.circe.examples.derivation

import io.circe.{ Decoder, Encoder }
import io.circe.derivation.{ deriveDecoder, deriveEncoder }
import io.circe.examples.scrooge.{ BiggerStruct, SomethingStruct }

object ScroogeInstances {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct1: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct1: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct1: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct1: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct2: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct2: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct2: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct2: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct3: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct3: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct3: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct3: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct4: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct4: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct4: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct4: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct5: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct5: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct5: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct5: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct6: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct6: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct6: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct6: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct7: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct7: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct7: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct7: Encoder.AsObject[BiggerStruct] = deriveEncoder
}
