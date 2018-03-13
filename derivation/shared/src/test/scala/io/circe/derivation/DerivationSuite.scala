package io.circe.derivation

import io.circe.{ Decoder, Encoder, ObjectEncoder }
import io.circe.examples.{ Bar, Baz, Foo, MultiParamListClass, Qux, SimpleClass }
import io.circe.testing.CodecTests

object DerivationSuiteCodecs extends Serializable {
  implicit val decodeFoo: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo: ObjectEncoder[Foo] = deriveEncoder
  implicit val decodeBar: Decoder[Bar] = deriveDecoder
  implicit val encodeBar: ObjectEncoder[Bar] = deriveEncoder
  implicit val decodeBaz: Decoder[Baz] = deriveDecoder
  implicit val encodeBaz: ObjectEncoder[Baz] = deriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: ObjectEncoder[Qux[A]] = deriveEncoder

  implicit val decodeMultiParamListClass: Decoder[MultiParamListClass] = deriveDecoder
  implicit val encodeMultiParamListClass: Encoder[MultiParamListClass] = deriveEncoder
  implicit val decodeSimpleClass: Decoder[SimpleClass] = deriveDecoder
  implicit val encodeSimpleClass: Encoder[SimpleClass] = deriveEncoder
}

class DerivationSuite extends CirceSuite {
  import DerivationSuiteCodecs._

  checkLaws("Codec[Foo]", CodecTests[Foo].codec)
  checkLaws("Codec[Bar]", CodecTests[Bar].codec)
  checkLaws("Codec[Baz]", CodecTests[Baz].codec)
  checkLaws("Codec[Qux[Baz]]", CodecTests[Qux[Baz]].codec)

  checkLaws("Codec[MultiParamListClass]", CodecTests[MultiParamListClass].codec)
  checkLaws("Codec[SimpleClass]", CodecTests[SimpleClass].codec)
}
