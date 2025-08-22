package io.circe.derivation

import io.circe.{ Codec, Decoder, Encoder, Json }
import io.circe.examples.{ Bar, Baz, Foo, Qux, Wibble }
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.testing.CodecTests
import scala.annotation.nowarn

object TransformMemberNamesSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder(renaming.snakeCase, true, None)
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder(renaming.snakeCase, None)
  val codecForFoo: Codec.AsObject[Foo] = deriveCodec(renaming.snakeCase, true, None)

  implicit val decodeBar: Decoder[Bar] = deriveDecoder(renaming.snakeCase, true, None)
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder(renaming.snakeCase, None)
  val codecForBar: Codec.AsObject[Bar] = deriveCodec(renaming.snakeCase, true, None)

  implicit val decodeBaz: Decoder[Baz] = deriveDecoder(renaming.snakeCase, true, None)
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder(renaming.snakeCase, None)
  val codecForBaz: Codec.AsObject[Baz] = deriveCodec(renaming.snakeCase, true, None)

  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] =
    deriveDecoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), true, None)
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] =
    deriveEncoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"), None)
  def codecForQux[A: Decoder: Encoder]: Codec.AsObject[Qux[A]] = deriveCodec(
    renaming.replaceWith("aa" -> "1", "bb" -> "2"),
    true,
    None
  )

  val transformNames = (s: String) => s.toUpperCase()
  @nowarn("msg=Failed to transform names at compile time. Transformation will happen at runtime on codec invocation instead. This is due to the limitations of scala.reflect.macros.blackbox.Context#eval")
  implicit val decodeWibble: Decoder[Wibble] =
    deriveDecoder(transformNames, true, None)
  @nowarn("msg=Failed to transform names at compile time. Transformation will happen at runtime on codec invocation instead. This is due to the limitations of scala.reflect.macros.blackbox.Context#eval")
  implicit val encodeWibble: Encoder[Wibble] =
    deriveEncoder(transformNames, None)
  @nowarn("msg=Failed to transform names at compile time. Transformation will happen at runtime on codec invocation instead. This is due to the limitations of scala.reflect.macros.blackbox.Context#eval")
  val codecForWibble: Codec.AsObject[Wibble] = deriveCodec(
    transformNames,
    true,
    None
  )

}

class TransformMemberNamesSuite extends CirceSuite {
  import TransformMemberNamesExample._
  import TransformMemberNamesSuiteCodecs._

  checkAll("Codec[Foo]", CodecTests[Foo].codec)
  checkAll("Codec[Foo] via Codec", CodecTests[Foo](codecForFoo, codecForFoo).codec)
  checkAll("Codec[Bar]", CodecTests[Bar].codec)
  checkAll("Codec[Bar] via Codec", CodecTests[Bar](codecForBar, codecForBar).codec)
  checkAll("Codec[Baz]", CodecTests[Baz].codec)
  checkAll("Codec[Baz] via Codec", CodecTests[Baz](codecForBaz, codecForBaz).codec)
  checkAll("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
  checkAll("Codec[Qux[Baz]] via Codec", CodecTests[Qux[Baz]](codecForQux, codecForQux).codec)
  checkAll("Codec[Wibble]", CodecTests[Wibble].codec)
  checkAll("Codec[Wibble] via Codec", CodecTests[Wibble](codecForWibble, codecForWibble).codec)

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

  checkAll(
    "CodecAgreementWithCodec[Wibble]",
    CodecAgreementTests[Wibble](
      codecForWibble,
      codecForWibble,
      decodeWibble,
      encodeWibble
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
