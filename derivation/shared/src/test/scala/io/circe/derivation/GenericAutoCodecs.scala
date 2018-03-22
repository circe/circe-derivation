package io.circe.derivation

import io.circe.{ Decoder, Encoder, ObjectEncoder }
import io.circe.examples.{ Bar, Baz, Foo, Qux, SimpleClass }
import io.circe.generic.semiauto.{
  deriveDecoder => genericDeriveDecoder,
  deriveEncoder => genericDeriveEncoder
}

object GenericAutoCodecs {
  implicit val decodeFoo: Decoder[Foo] = genericDeriveDecoder
  implicit val encodeFoo: ObjectEncoder[Foo] = genericDeriveEncoder
  implicit val decodeBar: Decoder[Bar] = genericDeriveDecoder
  implicit val encodeBar: ObjectEncoder[Bar] = genericDeriveEncoder
  implicit val decodeBaz: Decoder[Baz] = genericDeriveDecoder
  implicit val encodeBaz: ObjectEncoder[Baz] = genericDeriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = genericDeriveDecoder
  implicit def encodeQux[A: Encoder]: ObjectEncoder[Qux[A]] = genericDeriveEncoder

  implicit val decodeSimpleClass: Decoder[SimpleClass] = genericDeriveDecoder
  implicit val encodeSimpleClass: Encoder[SimpleClass] = genericDeriveEncoder
}
