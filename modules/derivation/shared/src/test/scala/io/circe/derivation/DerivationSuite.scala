package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples._
import io.circe.testing.CodecTests

object DerivationSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder
  val codecForFoo: Codec.AsObject[Foo] = deriveCodec

  implicit val decodeBar: Decoder[Bar] = deriveDecoder
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder
  val codecForBar: Codec.AsObject[Bar] = deriveCodec

  implicit val decodeBaz: Decoder[Baz] = deriveDecoder
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder
  val codecForBaz: Codec.AsObject[Baz] = deriveCodec

  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] = deriveEncoder
  def codecForQux[A: Encoder: Decoder]: Codec.AsObject[Qux[A]] = deriveCodec

  implicit val decodeMultiParamListClass: Decoder[MultiParamListClass] = deriveDecoder
  implicit val encodeMultiParamListClass: Encoder[MultiParamListClass] = deriveEncoder
  val codecForMultiParamListClass: Codec[MultiParamListClass] = deriveCodec

  implicit val decodeSimpleClass: Decoder[SimpleClass] = deriveDecoder
  implicit val encodeSimpleClass: Encoder[SimpleClass] = deriveEncoder
  val codecForSimpleClass: Codec[SimpleClass] = deriveCodec

  implicit val decodeCustomApplyParamNamesClass: Decoder[CustomApplyParamNamesClass] = deriveDecoder
  implicit val encodeCustomApplyParamNamesClass: Encoder[CustomApplyParamNamesClass] = deriveEncoder
  val codecForCustomApplyParamNamesClass: Codec[CustomApplyParamNamesClass] = deriveCodec

  implicit val decodeCustomApplyParamTypesClass: Decoder[CustomApplyParamTypesClass] = deriveDecoder
  implicit val encodeCustomApplyParamTypesClass: Encoder[CustomApplyParamTypesClass] = deriveEncoder
  val codecForCustomApplyParamTypesClass: Codec[CustomApplyParamTypesClass] = deriveCodec
}

class DerivationSuite extends CirceSuite {
  import DerivationSuiteCodecs._

  "deriveDecoder" should "only accept JSON objects for zero-member case classes" in forAll { (json: Json) =>
    case class EmptyCaseClass()

    val decodeEmptyCaseClass: Decoder[EmptyCaseClass] = deriveDecoder

    assert(decodeEmptyCaseClass.decodeJson(json).isRight === json.isObject)
  }

  "deriveCodec" should "only accept JSON objects for zero-member case classes" in forAll { (json: Json) =>
    case class EmptyCaseClass()

    val codecForEmptyCaseClass: Codec[EmptyCaseClass] = deriveCodec

    assert(codecForEmptyCaseClass.decodeJson(json).isRight === json.isObject)
  }

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Foo] via Codec", CodecTests[Foo](codecForFoo, codecForFoo).codec)

  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Bar] via Codec", CodecTests[Bar](codecForBar, codecForBar).codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Baz] via Codec", CodecTests[Baz](codecForBaz, codecForBaz).codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
  checkLaws("Codec[Qux[Baz]] via Codec", CodecTests[Qux[Baz]](codecForQux, codecForQux).codec)

  checkLaws("Codec[MultiParamListClass]", CodecTests[MultiParamListClass].codec)
  checkLaws(
    "Codec[MultiParamListClass] via Codec",
    CodecTests[MultiParamListClass](codecForMultiParamListClass, codecForMultiParamListClass).codec
  )
  checkLaws("Codec[SimpleClass]", CodecTests[SimpleClass].codec)
  checkLaws("Codec[SimpleClass] via Codec", CodecTests[SimpleClass](codecForSimpleClass, codecForSimpleClass).codec)
  checkLaws("Codec[CustomApplyParamNamesClass]", CodecTests[CustomApplyParamNamesClass].codec)
  checkLaws(
    "Codec[CustomApplyParamNamesClass] via Codec",
    CodecTests[CustomApplyParamNamesClass](codecForCustomApplyParamNamesClass, codecForCustomApplyParamNamesClass).codec
  )
  checkLaws("Codec[CustomApplyParamTypesClass]", CodecTests[CustomApplyParamTypesClass].codec)
  checkLaws(
    "Codec[CustomApplyParamTypesClass] via Codec",
    CodecTests[CustomApplyParamTypesClass](codecForCustomApplyParamTypesClass, codecForCustomApplyParamTypesClass).codec
  )

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

  checkLaws(
    "CodecAgreementWithCodec[Foo]",
    CodecAgreementTests[Foo](
      codecForFoo,
      codecForFoo,
      decodeFoo,
      encodeFoo
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[Bar]",
    CodecAgreementTests[Bar](
      codecForBar,
      codecForBar,
      decodeBar,
      encodeBar
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[Baz]",
    CodecAgreementTests[Baz](
      codecForBaz,
      codecForBaz,
      decodeBaz,
      encodeBaz
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[Qux[Baz]]",
    CodecAgreementTests[Qux[Baz]](
      codecForQux,
      codecForQux,
      decodeQux,
      encodeQux
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[SimpleClass]",
    CodecAgreementTests[SimpleClass](
      codecForSimpleClass,
      codecForSimpleClass,
      decodeSimpleClass,
      encodeSimpleClass
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[CustomApplyParamNamesClass]",
    CodecAgreementTests[CustomApplyParamNamesClass](
      codecForCustomApplyParamNamesClass,
      codecForCustomApplyParamNamesClass,
      decodeCustomApplyParamNamesClass,
      encodeCustomApplyParamNamesClass
    ).codecAgreement
  )

  checkLaws(
    "CodecAgreementWithCodec[CustomApplyParamTypesClass]",
    CodecAgreementTests[CustomApplyParamTypesClass](
      codecForCustomApplyParamTypesClass,
      codecForCustomApplyParamTypesClass,
      decodeCustomApplyParamTypesClass,
      encodeCustomApplyParamTypesClass
    ).codecAgreement
  )
}
