package io.circe.derivation

import io.circe.{ Decoder, Encoder, Json }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.testing.CodecTests

object NameTransformationSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder(renaming.snakeCase)
  implicit val encodeFoo: Encoder.AsObject[Foo] = deriveEncoder(renaming.snakeCase)
  implicit val decodeBar: Decoder[Bar] = deriveDecoder(renaming.snakeCase)
  implicit val encodeBar: Encoder.AsObject[Bar] = deriveEncoder(renaming.snakeCase)
  implicit val decodeBaz: Decoder[Baz] = deriveDecoder(renaming.snakeCase)
  implicit val encodeBaz: Encoder.AsObject[Baz] = deriveEncoder(renaming.snakeCase)
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"))
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] =
    deriveEncoder(renaming.replaceWith("aa" -> "1", "bb" -> "2"))
}

class NameTransformationSuite extends CirceSuite {
  import NameTransformationExample._
  import NameTransformationSuiteCodecs._

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)

  checkLaws("Codec[User]", CodecTests[User].codec)

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
