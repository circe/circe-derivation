package io.circe.derivation

import io.circe.{ Decoder, ObjectEncoder }
import io.circe.examples.{ Bar, Baz, Foo }
import io.circe.testing.CodecTests

class DerivationSuite extends CirceSuite {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo: ObjectEncoder[Foo] = deriveEncoder
  implicit val decodeBar: Decoder[Bar] = deriveDecoder
  implicit val encodeBar: ObjectEncoder[Bar] = deriveEncoder
  implicit val decodeBaz: Decoder[Baz] = deriveDecoder
  implicit val encodeBaz: ObjectEncoder[Baz] = deriveEncoder

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
}