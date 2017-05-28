package io.circe.derivation

import io.circe.{ Decoder, Encoder, ObjectEncoder }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.testing.CodecTests

class DerivationSuite extends CirceSuite {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo: ObjectEncoder[Foo] = deriveEncoder
  implicit val decodeBar: Decoder[Bar] = deriveDecoder
  implicit val encodeBar: ObjectEncoder[Bar] = deriveEncoder
  implicit val decodeBaz: Decoder[Baz] = deriveDecoder
  implicit val encodeBaz: ObjectEncoder[Baz] = deriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: ObjectEncoder[Qux[A]] = deriveEncoder

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)
}
