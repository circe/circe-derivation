package io.circe.derivation

import cats.data.Validated
import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.syntax._
import io.circe.examples._
import io.circe.testing.CodecTests

object TransformMemberNamesWithDefaultsSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder(renaming.snakeCase)
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder(renaming.snakeCase)
  val codecForFoo: Codec.AsObject[Foo] = deriveCodec(renaming.snakeCase)

  implicit val decodeBar: Decoder[Bar] = deriveDecoder(renaming.snakeCase)
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder(renaming.snakeCase)
  val codecForBar: Codec.AsObject[Bar] = deriveCodec(renaming.snakeCase)

  implicit val decodeBaz: Decoder[Baz] = deriveDecoder(renaming.snakeCase)
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder(renaming.snakeCase)
  val codecForBaz: Codec.AsObject[Baz] = deriveCodec(renaming.snakeCase)

  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] =
    deriveDecoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"))
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] =
    deriveEncoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"))
  def codecForQux[A: Decoder: Encoder]: Codec.AsObject[Qux[A]] = deriveCodec(
    renaming.replaceWith("aa" -> "1", "bb" -> "2")
  )

  implicit val decodeWithDefaults: Decoder[WithDefaults] = deriveDecoder(identity)
  implicit val encodeWithDefaults: Encoder[WithDefaults] = deriveEncoder(identity)
  val codecForWithDefaults: Codec[WithDefaults] = deriveCodec(identity)

}

class TransformMemberNamesWithDefaultsSuite extends CirceSuite {
  import TransformMemberNamesExample._
  import TransformMemberNamesWithDefaultsSuiteCodecs._

  checkAll("Codec[Foo]", CodecTests[Foo].codec)
  checkAll("Codec[Foo] via Codec", CodecTests[Foo](codecForFoo, codecForFoo).codec)
  checkAll("Codec[Bar]", CodecTests[Bar].codec)
  checkAll("Codec[Bar] via Codec", CodecTests[Bar](codecForBar, codecForBar).codec)
  checkAll("Codec[Baz]", CodecTests[Baz].codec)
  checkAll("Codec[Baz] via Codec", CodecTests[Baz](codecForBaz, codecForBaz).codec)
  checkAll("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
  checkAll("Codec[Qux[Baz]] via Codec", CodecTests[Qux[Baz]](codecForQux, codecForQux).codec)

  checkAll("Codec[User]", CodecTests[User].codec)
  checkAll("Codec[User] via Codec", CodecTests[User](User.codecForUser, User.codecForUser).codec)

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

  "deriveEncoder" should "respect default behaviour" in {
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
