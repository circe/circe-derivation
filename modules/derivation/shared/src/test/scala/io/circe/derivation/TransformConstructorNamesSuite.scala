package io.circe.derivation

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.examples.{Adt, AdtBar, AdtFoo, AdtQux}
import io.circe.syntax._
import io.circe.testing.CodecTests

object TransformConstructorNamesSuite extends Serializable {

  implicit val decodeAdtBar: Decoder[AdtBar] = deriveDecoder
  implicit val encodeAdtBar: Encoder.AsObject[AdtBar] = deriveEncoder

  implicit val decodeAdtFoo: Decoder[AdtFoo] = deriveDecoder
  implicit val encodeAdtFoo: Encoder.AsObject[AdtFoo] = deriveEncoder

  implicit val decodeAdtQux: Decoder[AdtQux.type] = deriveDecoder
  implicit val encodeAdtQux: Encoder.AsObject[AdtQux.type] = deriveEncoder

  implicit val decodeAdt: Decoder[Adt] = deriveDecoder(identity, renaming.snakeCase, true, None)
  implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder(identity, renaming.snakeCase, None)
  val codecForAdt: Codec[Adt] = deriveCodec(identity, renaming.snakeCase, true, None)

  object discriminator {
    val typeField = Some("_type")

    implicit val decodeAdt: Decoder[Adt] = deriveDecoder(identity, renaming.snakeCase, true, typeField)
    implicit val encodeAdt: Encoder.AsObject[Adt] = deriveEncoder(identity, renaming.snakeCase, typeField)
    val codecForAdt: Codec[Adt] = deriveCodec(identity, renaming.snakeCase, true, typeField)
  }
}

class TransformConstructorNamesSuite extends CirceSuite {
  import TransformConstructorNamesSuite._

  checkAll("Codec[Adt]", CodecTests[Adt].codec)
  checkAll("Codec[Adt] via Codec", CodecTests[Adt](codecForAdt, codecForAdt).codec)
  checkAll(
    "Codec[Adt] via Codec with discriminator",
    CodecTests[Adt](discriminator.codecForAdt, discriminator.codecForAdt).codec
  )
  checkAll(
    "CodecAgreementWithCodec[Adt]",
    CodecAgreementTests[Adt](codecForAdt, codecForAdt, decodeAdt, encodeAdt).codecAgreement
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

  "deriveEncoder" should "properly transform constructor names" in forAll { adt: Adt =>
    val expected = adt match {
      case AdtFoo(i, s) => Json.obj("adt_foo" -> Json.obj("i" -> i.asJson, "s" -> s.asJson))
      case AdtBar(xs) => Json.obj("adt_bar" -> Json.obj("xs" -> xs.asJson))
      case AdtQux => Json.obj("adt_qux" -> Json.obj())
    }

    assert(adt.asJson === expected)
  }

  it should "properly transform constructor names when using a discriminator" in forAll { adt: Adt =>
    val expected = adt match {
      case AdtFoo(i, s) => Json.obj("i" -> i.asJson, "s" -> s.asJson, "_type" -> "adt_foo".asJson)
      case AdtBar(xs) => Json.obj("xs" -> xs.asJson, "_type" -> "adt_bar".asJson)
      case AdtQux => Json.obj("_type" -> "adt_qux".asJson)
    }

    assert(adt.asJson(discriminator.encodeAdt) === expected)
  }
}
