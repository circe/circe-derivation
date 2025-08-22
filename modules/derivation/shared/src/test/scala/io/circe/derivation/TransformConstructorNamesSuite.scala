package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples.{ Adt, AdtBar, AdtFoo, AdtQux, NestedAdt, NestedAdtBar, NestedAdtFoo, NestedAdtQux }
import io.circe.syntax._
import io.circe.testing.CodecTests

object TransformConstructorNamesSuite extends Serializable {

  implicit val decodeAdtBar: Decoder[AdtBar] = deriveDecoder
  implicit val encodeAdtBar: Encoder.AsObject[AdtBar] = deriveEncoder

  implicit val decodeAdtFoo: Decoder[AdtFoo] = deriveDecoder
  implicit val encodeAdtFoo: Encoder.AsObject[AdtFoo] = deriveEncoder

  implicit val decodeAdtQux: Decoder[AdtQux.type] = deriveDecoder
  implicit val encodeAdtQux: Encoder.AsObject[AdtQux.type] = deriveEncoder

  implicit val decodeAdt: Decoder[Adt] = deriveDecoder(renaming.snakeCase, true, None, false)
  implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder(renaming.snakeCase, None)
  val codecForAdt: Codec[Adt] = deriveCodec(renaming.snakeCase, true, None, false)

  implicit val decodeNestedAdtBar: Decoder[NestedAdtBar] = deriveDecoder
  implicit val encodeNestedAdtBar: Encoder.AsObject[NestedAdtBar] = deriveEncoder

  implicit val decodeNestedAdtFoo: Decoder[NestedAdtFoo] = deriveDecoder
  implicit val encodeNestedAdtFoo: Encoder.AsObject[NestedAdtFoo] = deriveEncoder

  implicit val decodeNestedAdtQux: Decoder[NestedAdtQux.type] = deriveDecoder
  implicit val encodeNestedAdtQux: Encoder.AsObject[NestedAdtQux.type] = deriveEncoder

  implicit val decodeNestedAdt: Decoder[NestedAdt] = deriveDecoder(renaming.snakeCase, true, None, false)
  implicit val encodeNestedAdt: Encoder.AsObject[NestedAdt] = deriveEncoder(renaming.snakeCase, None)
  val codecForNestedAdt: Codec[NestedAdt] = deriveCodec(renaming.snakeCase, true, None, false)

  object discriminator {
    val typeField = Some("_type")

    implicit val decodeAdt: Decoder[Adt] = deriveDecoder(renaming.snakeCase, true, typeField, false)
    implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder(renaming.snakeCase, typeField)
    val codecForAdt: Codec[Adt] = deriveCodec(renaming.snakeCase, true, typeField, false)

    implicit val decodeNestedAdt: Decoder[NestedAdt] = deriveDecoder(renaming.snakeCase, true, typeField, false)
    implicit val encodeNestedAdt: Encoder.AsObject[NestedAdt] = deriveEncoder(renaming.snakeCase, typeField)
    val codecForNestedAdt: Codec[NestedAdt] = deriveCodec(renaming.snakeCase, true, typeField, false)
  }
}

class TransformConstructorNamesSuite extends CirceSuite {
  import TransformConstructorNamesSuite._

  checkAll("Codec[Adt]", CodecTests[Adt].codec)
  checkAll("Codec[NestedAdt]", CodecTests[NestedAdt].codec)
  checkAll("Codec[Adt] via Codec", CodecTests[Adt](codecForAdt, codecForAdt).codec)
  checkAll("Codec[NestedAdt] via Codec", CodecTests[NestedAdt](codecForNestedAdt, codecForNestedAdt).codec)
  checkAll(
    "Codec[Adt] via Codec with discriminator",
    CodecTests[Adt](discriminator.codecForAdt, discriminator.codecForAdt).codec
  )
  checkAll(
    "Codec[NestedAdt] via Codec with discriminator",
    CodecTests[NestedAdt](discriminator.codecForNestedAdt, discriminator.codecForNestedAdt).codec
  )
  checkAll(
    "CodecAgreementWithCodec[Adt]",
    CodecAgreementTests[Adt](codecForAdt, codecForAdt, decodeAdt, encodeAdt).codecAgreement
  )
  checkAll(
    "CodecAgreementWithCodec[NestedAdt]",
    CodecAgreementTests[NestedAdt](
      codecForNestedAdt,
      codecForNestedAdt,
      decodeNestedAdt,
      encodeNestedAdt
    ).codecAgreement
  )
  checkAll(
    "CodecAgreementWithCodec[Adt] with discriminator",
    CodecAgreementTests(
      discriminator.codecForAdt,
      discriminator.codecForAdt,
      discriminator.decodeAdt,
      discriminator.encodeAdt
    ).codecAgreement
  )
  checkAll(
    "CodecAgreementWithCodec[NestedAdt] with discriminator",
    CodecAgreementTests(
      discriminator.codecForNestedAdt,
      discriminator.codecForNestedAdt,
      discriminator.decodeNestedAdt,
      discriminator.encodeNestedAdt
    ).codecAgreement
  )

  "deriveEncoder" should "properly transform constructor names" in forAll { adt: Adt =>
    val expected = adt match {
      case AdtFoo(i, s) => Json.obj("adt_foo" -> Json.obj("i" -> i.asJson, "s" -> s.asJson))
      case AdtBar(xs)   => Json.obj("adt_bar" -> Json.obj("xs" -> xs.asJson))
      case AdtQux       => Json.obj("adt_qux" -> Json.obj())
    }

    assert(adt.asJson === expected)
  }

  "deriveEncoder" should "properly transform nested constructor names" in forAll { adt: NestedAdt =>
    val expected = adt match {
      case NestedAdtFoo(i, s) => Json.obj("nested_adt_foo" -> Json.obj("i" -> i.asJson, "s" -> s.asJson))
      case NestedAdtBar(xs)   => Json.obj("nested_adt_bar" -> Json.obj("xs" -> xs.asJson))
      case NestedAdtQux       => Json.obj("nested_adt_qux" -> Json.obj())
    }

    assert(adt.asJson === expected)
  }

  it should "properly transform constructor names when using a discriminator" in forAll { adt: Adt =>
    val expected = adt match {
      case AdtFoo(i, s) => Json.obj("i" -> i.asJson, "s" -> s.asJson, "_type" -> "adt_foo".asJson)
      case AdtBar(xs)   => Json.obj("xs" -> xs.asJson, "_type" -> "adt_bar".asJson)
      case AdtQux       => Json.obj("_type" -> "adt_qux".asJson)
    }

    assert(adt.asJson(discriminator.encodeAdt) === expected)
  }

  it should "properly transform nested constructor names when using a discriminator" in forAll { adt: NestedAdt =>
    val expected = adt match {
      case NestedAdtFoo(i, s) => Json.obj("i" -> i.asJson, "s" -> s.asJson, "_type" -> "nested_adt_foo".asJson)
      case NestedAdtBar(xs)   => Json.obj("xs" -> xs.asJson, "_type" -> "nested_adt_bar".asJson)
      case NestedAdtQux       => Json.obj("_type" -> "nested_adt_qux".asJson)
    }

    assert(adt.asJson(discriminator.encodeNestedAdt) === expected)
  }
}
