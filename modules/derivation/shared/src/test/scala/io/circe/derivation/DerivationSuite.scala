package io.circe.derivation

import cats.data.Validated
import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples._
import io.circe.syntax._
import io.circe.testing.CodecTests

object DerivationSuiteCodecs extends Serializable {
  private[this] val scala, Any, String, Unit, Nil = ()
  private[this] trait scala; private[this] trait Any; private[this] trait String; private[this] trait Unit;
  private[this] trait List; private[this] trait Option

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

  implicit val decodeWithDefaults: Decoder[WithDefaults] = deriveDecoder(identity, identity, true, None)
  implicit val encodeWithDefaults: Encoder[WithDefaults] = deriveEncoder(identity, identity, None)
  val codecForWithDefaults: Codec[WithDefaults] = deriveCodec(identity, identity, true, None)

  implicit val decodeWithJson: Decoder[WithJson] = deriveDecoder(identity, identity, true, None)
  implicit val encodeWithJson: Encoder[WithJson] = deriveEncoder(identity, identity, None)
  val codecForWithJson: Codec[WithJson] = deriveCodec(identity, identity, true, None)

  implicit val decodeAdtFoo: Decoder[AdtFoo] = deriveDecoder
  implicit val encodeAdtFoo: Encoder.AsObject[AdtFoo] = deriveEncoder

  implicit val decodeAdtBar: Decoder[AdtBar] = deriveDecoder
  implicit val encodeAdtBar: Encoder.AsObject[AdtBar] = deriveEncoder

  implicit val decodeAdtQux: Decoder[AdtQux.type] = deriveDecoder
  implicit val encodeAdtQux: Encoder.AsObject[AdtQux.type] = deriveEncoder

  implicit val decodeAdt: Decoder[Adt] = deriveDecoder
  implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder
  val codecForAdt: Codec[Adt] = deriveCodec

  object discriminator {
    val typeField = Some("_type")
    implicit val decodeAdtFoo: Decoder[AdtFoo] = deriveDecoder(identity, identity, false, typeField)
    implicit val encodeAdtFoo: Encoder.AsObject[AdtFoo] = deriveEncoder(identity, identity, typeField)

    implicit val decodeAdtBar: Decoder[AdtBar] = deriveDecoder(identity, identity, false, typeField)
    implicit val encodeAdtBar: Encoder.AsObject[AdtBar] = deriveEncoder(identity, identity, typeField)

    implicit val decodeAdtQux: Decoder[AdtQux.type] = deriveDecoder(identity, identity, false, typeField)
    implicit val encodeAdtQux: Encoder.AsObject[AdtQux.type] = deriveEncoder(identity, identity, typeField)

    implicit val decodeAdt: Decoder[Adt] = deriveDecoder(identity, identity, false, typeField)
    implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder(identity, identity, typeField)
    val codecForAdt: Codec[Adt] = deriveCodec(identity, identity, false, typeField)
  }
}

class DerivationSuite extends CirceSuite {
  import DerivationSuiteCodecs._

  "deriveDecoder" should "only accept JSON objects for zero-member case classes" in forAll { (json: Json) =>
    case class EmptyCaseClass()

    val decodeEmptyCaseClass: Decoder[EmptyCaseClass] = deriveDecoder

    assert(decodeEmptyCaseClass.decodeJson(json).isRight === json.isObject)
  }

  it should "fail to decode ADTs with an invalid object wrapper" in {
    assert(decodeAdt.decodeJson(Json.obj("adtt" -> AdtFoo(1).asJson)).isLeft)
  }

  it should "fail to decode ADTs with an invalid discriminator" in {
    val withBadDiscriminator = AdtFoo(1).asJsonObject.add("_type", "adtt".asJson)
    assert(discriminator.decodeAdt.decodeJson(withBadDiscriminator.asJson).isLeft)
  }

  "deriveCodec" should "only accept JSON objects for zero-member case classes" in forAll { (json: Json) =>
    case class EmptyCaseClass()

    val codecForEmptyCaseClass: Codec[EmptyCaseClass] = deriveCodec

    assert(codecForEmptyCaseClass.decodeJson(json).isRight === json.isObject)
  }

  it should "fail to decode ADTs with an invalid object wrapper" in {
    assert(codecForAdt.decodeJson(Json.obj("adtt" -> AdtFoo(1).asJson)).isLeft)
  }

  it should "fail to decode ADTs with an invalid discriminator" in {
    val withBadDiscriminator = AdtFoo(1).asJsonObject.add("_type", "adtt".asJson)
    assert(discriminator.codecForAdt.decodeJson(withBadDiscriminator.asJson).isLeft)
  }

  checkAll("Codec[Foo]", CodecTests[Foo].codec)
  checkAll("Codec[Foo] via Codec", CodecTests[Foo](codecForFoo, codecForFoo).codec)

  checkAll("Codec[Bar]", CodecTests[Bar].codec)
  checkAll("Codec[Bar] via Codec", CodecTests[Bar](codecForBar, codecForBar).codec)
  checkAll("Codec[Baz]", CodecTests[Baz].codec)
  checkAll("Codec[Baz] via Codec", CodecTests[Baz](codecForBaz, codecForBaz).codec)
  checkAll("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
  checkAll("Codec[Qux[Baz]] via Codec", CodecTests[Qux[Baz]](codecForQux, codecForQux).codec)

  checkAll("Codec[MultiParamListClass]", CodecTests[MultiParamListClass].codec)
  checkAll(
    "Codec[MultiParamListClass] via Codec",
    CodecTests[MultiParamListClass](codecForMultiParamListClass, codecForMultiParamListClass).codec
  )
  checkAll("Codec[SimpleClass]", CodecTests[SimpleClass].codec)
  checkAll("Codec[SimpleClass] via Codec", CodecTests[SimpleClass](codecForSimpleClass, codecForSimpleClass).codec)
  checkAll("Codec[CustomApplyParamNamesClass]", CodecTests[CustomApplyParamNamesClass].codec)
  checkAll(
    "Codec[CustomApplyParamNamesClass] via Codec",
    CodecTests[CustomApplyParamNamesClass](codecForCustomApplyParamNamesClass, codecForCustomApplyParamNamesClass).codec
  )
  checkAll("Codec[CustomApplyParamTypesClass]", CodecTests[CustomApplyParamTypesClass].codec)
  checkAll(
    "Codec[CustomApplyParamTypesClass] via Codec",
    CodecTests[CustomApplyParamTypesClass](codecForCustomApplyParamTypesClass, codecForCustomApplyParamTypesClass).codec
  )

  checkAll(
    "CodecAgreement[Foo]",
    CodecAgreementTests[Foo](
      GenericAutoCodecs.decodeFoo,
      GenericAutoCodecs.encodeFoo,
      decodeFoo,
      encodeFoo
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[Bar]",
    CodecAgreementTests[Bar](
      GenericAutoCodecs.decodeBar,
      GenericAutoCodecs.encodeBar,
      decodeBar,
      encodeBar
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[Baz]",
    CodecAgreementTests[Baz](
      GenericAutoCodecs.decodeBaz,
      GenericAutoCodecs.encodeBaz,
      decodeBaz,
      encodeBaz
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[Qux[Baz]]",
    CodecAgreementTests[Qux[Baz]](
      GenericAutoCodecs.decodeQux[Baz],
      GenericAutoCodecs.encodeQux[Baz],
      decodeQux[Baz],
      encodeQux[Baz]
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[SimpleClass]",
    CodecAgreementTests[SimpleClass](
      GenericAutoCodecs.decodeSimpleClass,
      GenericAutoCodecs.encodeSimpleClass,
      decodeSimpleClass,
      encodeSimpleClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[CustomApplyParamNamesClass]",
    CodecAgreementTests[CustomApplyParamNamesClass](
      GenericAutoCodecs.decodeCustomApplyParamNamesClass,
      GenericAutoCodecs.encodeCustomApplyParamNamesClass,
      decodeCustomApplyParamNamesClass,
      encodeCustomApplyParamNamesClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[CustomApplyParamTypesClass]",
    CodecAgreementTests[CustomApplyParamTypesClass](
      GenericAutoCodecs.decodeCustomApplyParamTypesClass,
      GenericAutoCodecs.encodeCustomApplyParamTypesClass,
      decodeCustomApplyParamTypesClass,
      encodeCustomApplyParamTypesClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreement[Adt]",
    CodecAgreementTests[Adt](
      GenericAutoCodecs.decodeAdt,
      GenericAutoCodecs.encodeAdt,
      decodeAdt,
      encodeAdt
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[Foo]",
    CodecAgreementTests[Foo](
      codecForFoo,
      codecForFoo,
      decodeFoo,
      encodeFoo
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[Bar]",
    CodecAgreementTests[Bar](
      codecForBar,
      codecForBar,
      decodeBar,
      encodeBar
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[Baz]",
    CodecAgreementTests[Baz](
      codecForBaz,
      codecForBaz,
      decodeBaz,
      encodeBaz
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[Qux[Baz]]",
    CodecAgreementTests[Qux[Baz]](
      codecForQux,
      codecForQux,
      decodeQux,
      encodeQux
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[SimpleClass]",
    CodecAgreementTests[SimpleClass](
      codecForSimpleClass,
      codecForSimpleClass,
      decodeSimpleClass,
      encodeSimpleClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[CustomApplyParamNamesClass]",
    CodecAgreementTests[CustomApplyParamNamesClass](
      codecForCustomApplyParamNamesClass,
      codecForCustomApplyParamNamesClass,
      decodeCustomApplyParamNamesClass,
      encodeCustomApplyParamNamesClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[CustomApplyParamTypesClass]",
    CodecAgreementTests[CustomApplyParamTypesClass](
      codecForCustomApplyParamTypesClass,
      codecForCustomApplyParamTypesClass,
      decodeCustomApplyParamTypesClass,
      encodeCustomApplyParamTypesClass
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[WithDefaults]",
    CodecAgreementTests[WithDefaults](
      codecForWithDefaults,
      codecForWithDefaults,
      decodeWithDefaults,
      encodeWithDefaults
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[WithJson]",
    CodecAgreementTests[WithJson](
      codecForWithJson,
      codecForWithJson,
      decodeWithJson,
      encodeWithJson
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[Adt]",
    CodecAgreementTests[Adt](
      discriminator.codecForAdt,
      discriminator.codecForAdt,
      discriminator.decodeAdt,
      discriminator.encodeAdt
    ).codecAgreement
  )

  "useDefaults" should "cause defaults to be used for missing fields" in {
    val expectedBothDefaults = WithDefaults(0, 1, List(""))
    val expectedOneDefault = WithDefaults(0, 1, Nil)

    val j1 = Json.obj("i" := 0)
    val j2 = Json.obj("i" := 0, "k" := List.empty[String])
    val j3 = Json.obj("i" := 0, "k" := Json.Null)

    assert(decodeWithDefaults.decodeJson(j1) === Right(expectedBothDefaults))
    assert(codecForWithDefaults.decodeJson(j1) === Right(expectedBothDefaults))
    assert(decodeWithDefaults.decodeJson(j2) === Right(expectedOneDefault))
    assert(codecForWithDefaults.decodeJson(j2) === Right(expectedOneDefault))
    assert(decodeWithDefaults.decodeJson(j3) === Right(expectedBothDefaults))
    assert(codecForWithDefaults.decodeJson(j3) === Right(expectedBothDefaults))

    assert(decodeWithDefaults.decodeAccumulating(j1.hcursor) === Validated.validNel(expectedBothDefaults))
    assert(codecForWithDefaults.decodeAccumulating(j1.hcursor) === Validated.validNel(expectedBothDefaults))
    assert(decodeWithDefaults.decodeAccumulating(j2.hcursor) === Validated.validNel(expectedOneDefault))
    assert(codecForWithDefaults.decodeAccumulating(j2.hcursor) === Validated.validNel(expectedOneDefault))
    assert(decodeWithDefaults.decodeAccumulating(j3.hcursor) === Validated.validNel(expectedBothDefaults))
    assert(codecForWithDefaults.decodeAccumulating(j3.hcursor) === Validated.validNel(expectedBothDefaults))
  }
}
