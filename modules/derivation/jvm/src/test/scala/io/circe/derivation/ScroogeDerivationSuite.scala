package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder }
import io.circe.examples.scrooge._
import io.circe.testing.CodecTests

object ScroogeDerivationSuiteCodecs extends Serializable {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val codecForSomethingStruct: Codec.AsObject[SomethingStruct] = deriveCodec

  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = deriveEncoder
  val codecForBiggerStruct: Codec.AsObject[BiggerStruct] = deriveCodec
}

class ScroogeDerivationSuite extends CirceSuite {
  import ScroogeDerivationSuiteCodecs._

  checkAll("Codec[SomethingStruct]", CodecTests[SomethingStruct].codec)
  checkAll(
    "Codec[SomethingStruct] via Codec",
    CodecTests[SomethingStruct](codecForSomethingStruct, codecForSomethingStruct).codec
  )
  checkAll("Codec[BiggerStruct]", CodecTests[BiggerStruct].codec)
  checkAll("Codec[BiggerStruct] via Codec", CodecTests[BiggerStruct](codecForBiggerStruct, codecForBiggerStruct).codec)

  checkAll(
    "CodecAgreement[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      ScroogeGenericAutoCodecs.decodeSomethingStruct,
      ScroogeGenericAutoCodecs.encodeSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      codecForSomethingStruct,
      codecForSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[BiggerStruct]",
    CodecAgreementTests[BiggerStruct](
      codecForBiggerStruct,
      codecForBiggerStruct,
      decodeBiggerStruct,
      encodeBiggerStruct
    ).codecAgreement
  )
}
