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

  checkLaws("Codec[SomethingStruct]", CodecTests[SomethingStruct].codec)
  checkLaws(
    "Codec[SomethingStruct] via Codec",
    CodecTests[SomethingStruct](codecForSomethingStruct, codecForSomethingStruct).codec
  )
  checkLaws("Codec[BiggerStruct]", CodecTests[BiggerStruct].codec)
  checkLaws("Codec[BiggerStruct] via Codec", CodecTests[BiggerStruct](codecForBiggerStruct, codecForBiggerStruct).codec)

  checkLaws(
    "CodecAgreement[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      ScroogeGenericAutoCodecs.decodeSomethingStruct,
      ScroogeGenericAutoCodecs.encodeSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      codecForSomethingStruct,
      codecForSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[BiggerStruct]",
    CodecAgreementTests[BiggerStruct](
      codecForBiggerStruct,
      codecForBiggerStruct,
      decodeBiggerStruct,
      encodeBiggerStruct
    ).codecAgreement
  )
}
