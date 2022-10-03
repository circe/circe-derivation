package io.circe.derivation

import cats.data.{NonEmptyList, Validated}
import io.circe.examples.{Bar, Baz, Foo, Qux}
import io.circe.{Codec, CursorOp, Decoder, DecodingFailure, Encoder, Json}
import io.circe.testing.CodecTests

object StrictDecodingSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder(renaming.snakeCase, true, None, true)
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder(renaming.snakeCase, None)
  val codecForFoo: Codec.AsObject[Foo] = deriveCodec(renaming.snakeCase, true, None, true)

  implicit val decodeBar: Decoder[Bar] = deriveDecoder(renaming.snakeCase, true, None, true)
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder(renaming.snakeCase, None)
  val codecForBar: Codec.AsObject[Bar] = deriveCodec(renaming.snakeCase, true, None, true)

  implicit val decodeBaz: Decoder[Baz] = deriveDecoder(renaming.snakeCase, true, None, true)
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder(renaming.snakeCase, None)
  val codecForBaz: Codec.AsObject[Baz] = deriveCodec(renaming.snakeCase, true, None, true)

  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] =
    deriveDecoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), true, None, true)
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] =
    deriveEncoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), None)
  def codecForQux[A: Decoder: Encoder]: Codec.AsObject[Qux[A]] = deriveCodec(
    renaming.replaceWith("aa" -> "1", "bb" -> "2"),
    true,
    None,
    true
  )
}

class StrictDecodingSuite extends CirceSuite {
  import StrictDecodingExample._
  import StrictDecodingSuiteCodecs._

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

  "deriveDecoder" should "return error when json has extra fields" in {
    val j1 = Json.obj(
      "first_name" -> Json.fromString("John"),
      "last_name" -> Json.fromString("Smith"),
      "role" -> Json.obj("TITLE" -> Json.fromString("Entrepreneur")),
      "address" -> Json.obj(
        "#" -> Json.fromInt(5),
        "street" -> Json.fromString("Elm Street"),
        "city" -> Json.fromString("Springfield"),
        "foo" -> Json.fromString("bar")
      )
    )
    val j2 = Json.obj(
      "unexpected1" -> Json.fromInt(1),
      "unexpected2" -> Json.fromInt(2),
      "first_name" -> Json.fromString("John"),
      "last_name" -> Json.fromString("Smith")
    )

    val expectedFailure1 = DecodingFailure("Unexpected field: [foo]; valid fields: #, street, city", List(CursorOp.DownField("address")))
    val expectedFailure2 = DecodingFailure(
      "Unexpected field: [unexpected1]; valid fields: first_name, last_name, role, address",
      List.empty
    )

    assert(j1.as[User] === Left(expectedFailure1))
    assert(j2.as[User] === Left(expectedFailure2))

    val expectedFailureNel1 = NonEmptyList.of(
      expectedFailure1,
    )
    val expectedFailureNel2 = NonEmptyList.of(
      expectedFailure2,
      DecodingFailure("Unexpected field: [unexpected2]; valid fields: first_name, last_name, role, address", List.empty),
      DecodingFailure("Attempt to decode value on failed cursor", List(CursorOp.DownField("role"))),
      DecodingFailure("Attempt to decode value on failed cursor", List(CursorOp.DownField("address")))
    )

    assert(User.decodeUser.decodeAccumulating(j1.hcursor) === Validated.invalid(expectedFailureNel1))
    assert(User.decodeUser.decodeAccumulating(j2.hcursor) === Validated.invalid(expectedFailureNel2))
  }
}
