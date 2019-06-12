package io.circe.derivation

import io.circe.{ Decoder, Encoder }
import io.circe.examples._
import io.circe.testing.CodecTests

object DerivationSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder
  implicit val decodeBar: Decoder[Bar] = deriveDecoder
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder
  implicit val decodeBaz: Decoder[Baz] = deriveDecoder
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] = deriveEncoder

  implicit val decodeMultiParamListClass: Decoder[MultiParamListClass] = deriveDecoder
  implicit val encodeMultiParamListClass: Encoder[MultiParamListClass] = deriveEncoder
  implicit val decodeSimpleClass: Decoder[SimpleClass] = deriveDecoder
  implicit val encodeSimpleClass: Encoder[SimpleClass] = deriveEncoder
  implicit val decodeCustomApplyParamNamesClass: Decoder[CustomApplyParamNamesClass] = deriveDecoder
  implicit val encodeCustomApplyParamNamesClass: Encoder[CustomApplyParamNamesClass] = deriveEncoder
  implicit val decodeCustomApplyParamTypesClass: Decoder[CustomApplyParamTypesClass] = deriveDecoder
  implicit val encodeCustomApplyParamTypesClass: Encoder[CustomApplyParamTypesClass] = deriveEncoder
}

class DerivationSuite extends CirceSuite {
  import DerivationSuiteCodecs._

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)

  checkLaws("Codec[MultiParamListClass]", CodecTests[MultiParamListClass].codec)
  checkLaws("Codec[SimpleClass]", CodecTests[SimpleClass].codec)
  checkLaws("Codec[CustomApplyParamNamesClass]", CodecTests[CustomApplyParamNamesClass].codec)
  checkLaws("Codec[CustomApplyParamTypesClass]", CodecTests[CustomApplyParamTypesClass].codec)

  checkLaws(
    "CodecAgreement[Foo]",
    CodecAgreementTests[Foo](
      GenericAutoCodecs.decodeFoo,
      GenericAutoCodecs.encodeFoo,
      decodeFoo,
      encodeFoo
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[Bar]",
    CodecAgreementTests[Bar](
      GenericAutoCodecs.decodeBar,
      GenericAutoCodecs.encodeBar,
      decodeBar,
      encodeBar
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[Baz]",
    CodecAgreementTests[Baz](
      GenericAutoCodecs.decodeBaz,
      GenericAutoCodecs.encodeBaz,
      decodeBaz,
      encodeBaz
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[Qux[Baz]]",
    CodecAgreementTests[Qux[Baz]](
      GenericAutoCodecs.decodeQux[Baz],
      GenericAutoCodecs.encodeQux[Baz],
      decodeQux[Baz],
      encodeQux[Baz]
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[SimpleClass]",
    CodecAgreementTests[SimpleClass](
      GenericAutoCodecs.decodeSimpleClass,
      GenericAutoCodecs.encodeSimpleClass,
      decodeSimpleClass,
      encodeSimpleClass
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[CustomApplyParamNamesClass]",
    CodecAgreementTests[CustomApplyParamNamesClass](
      GenericAutoCodecs.decodeCustomApplyParamNamesClass,
      GenericAutoCodecs.encodeCustomApplyParamNamesClass,
      decodeCustomApplyParamNamesClass,
      encodeCustomApplyParamNamesClass
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreement[CustomApplyParamTypesClass]",
    CodecAgreementTests[CustomApplyParamTypesClass](
      GenericAutoCodecs.decodeCustomApplyParamTypesClass,
      GenericAutoCodecs.encodeCustomApplyParamTypesClass,
      decodeCustomApplyParamTypesClass,
      encodeCustomApplyParamTypesClass
    ).codecAgreement
  )
}
