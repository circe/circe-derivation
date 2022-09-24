/*
 * Copyright 2022 circe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.circe.derivation

import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.examples.Bar
import io.circe.examples.Baz
import io.circe.examples.Foo
import io.circe.examples.Qux
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.testing.CodecTests

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
