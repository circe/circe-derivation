package io.circe.derivation

import io.circe.{ Decoder, Encoder }
import io.circe.examples.scrooge._
import io.circe.testing.CodecTests

object ScroogeDerivationSuiteCodecs extends Serializable {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = deriveEncoder
}

class ScroogeDerivationSuite extends CirceSuite {
  import ScroogeDerivationSuiteCodecs._

  checkLaws("Codec[SomethingStruct]", CodecTests[SomethingStruct].codec)
  checkLaws("Codec[BiggerStruct]", CodecTests[BiggerStruct].codec)

  checkLaws(
    "CodecAgreement[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      ScroogeGenericAutoCodecs.decodeSomethingStruct,
      ScroogeGenericAutoCodecs.encodeSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )
}
