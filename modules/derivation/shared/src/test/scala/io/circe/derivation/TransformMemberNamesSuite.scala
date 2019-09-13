package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.testing.CodecTests

object TransformMemberNamesSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder(renaming.snakeCase, identity, true, None)
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder(renaming.snakeCase, identity, None)
  val codecForFoo: Codec.AsObject[Foo] = deriveCodec(renaming.snakeCase, identity, true, None)

  implicit val decodeBar: Decoder[Bar] = deriveDecoder(renaming.snakeCase, identity, true, None)
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder(renaming.snakeCase, identity, None)
  val codecForBar: Codec.AsObject[Bar] = deriveCodec(renaming.snakeCase, identity, true, None)

  implicit val decodeBaz: Decoder[Baz] = deriveDecoder(renaming.snakeCase, identity, true, None)
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder(renaming.snakeCase, identity, None)
  val codecForBaz: Codec.AsObject[Baz] = deriveCodec(renaming.snakeCase, identity, true, None)

  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] =
    deriveDecoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), identity, true, None)
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] =
    deriveEncoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), identity, None)
  def codecForQux[A: Decoder: Encoder]: Codec.AsObject[Qux[A]] = deriveCodec(
    renaming.replaceWith("aa" -> "1", "bb" -> "2"),
    identity,
    true,
    None
  )
}

class TransformMemberNamesSuite extends CirceSuite {
  import TransformMemberNamesExample._
  import TransformMemberNamesSuiteCodecs._

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Foo] via Codec", CodecTests[Foo](codecForFoo, codecForFoo).codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Bar] via Codec", CodecTests[Bar](codecForBar, codecForBar).codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Baz] via Codec", CodecTests[Baz](codecForBaz, codecForBaz).codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
  checkLaws("Codec[Qux[Baz]] via Codec", CodecTests[Qux[Baz]](codecForQux, codecForQux).codec)

  checkLaws("Codec[User]", CodecTests[User].codec)
  checkLaws("Codec[User] via Codec", CodecTests[User](User.codecForUser, User.codecForUser).codec)

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

  "deriveEncoder" should "properly transform member names" in forAll { (user: User) =>
    val expected = Json.obj(
      "first_name" -> Json.fromString(user.firstName),
      "last_name" -> Json.fromString(user.lastName),
      "role" -> Json.obj("TITLE" -> Json.fromString(user.role.title)),
      "address" -> Json.obj(
        "#" -> Json.fromInt(user.address.number),
        "street" -> Json.fromString(user.address.street),
        "city" -> Json.fromString(user.address.city)
      )
    )

    assert(user.asJson === expected)
  }

  it should "encode the last name-duplicated member when transformation isn't unique" in forAll { (abc: Abc) =>
    assert(abc.asJson === Json.obj("x" -> Json.fromString(abc.c)))
  }

  "deriveDecoder" should "decode appropriately when transformation isn't unique" in forAll { (abc: Abc) =>
    assert(decode[Abc](abc.asJson.noSpaces) === Right(Abc(abc.c, abc.c, abc.c)))
  }
}
