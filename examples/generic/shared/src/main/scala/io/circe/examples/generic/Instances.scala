package io.circe.examples.generic

import io.circe.{ Decoder, Encoder }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

object Instances {
  implicit val decodeFoo0: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo0: Encoder.AsObject[Foo] = deriveEncoder
  implicit val decodeBar0: Decoder[Bar] = deriveDecoder
  implicit val encodeBar0: Encoder.AsObject[Bar] = deriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] = deriveEncoder

  val decodeBaz0: Decoder[Baz] = deriveDecoder
  val encodeBaz0: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo1: Decoder[Foo] = deriveDecoder
  val encodeFoo1: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar1: Decoder[Bar] = deriveDecoder
  val encodeBar1: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz1: Decoder[Baz] = deriveDecoder
  val encodeBaz1: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo2: Decoder[Foo] = deriveDecoder
  val encodeFoo2: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar2: Decoder[Bar] = deriveDecoder
  val encodeBar2: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz2: Decoder[Baz] = deriveDecoder
  val encodeBaz2: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo3: Decoder[Foo] = deriveDecoder
  val encodeFoo3: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar3: Decoder[Bar] = deriveDecoder
  val encodeBar3: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz3: Decoder[Baz] = deriveDecoder
  val encodeBaz3: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo4: Decoder[Foo] = deriveDecoder
  val encodeFoo4: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar4: Decoder[Bar] = deriveDecoder
  val encodeBar4: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz4: Decoder[Baz] = deriveDecoder
  val encodeBaz4: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo5: Decoder[Foo] = deriveDecoder
  val encodeFoo5: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar5: Decoder[Bar] = deriveDecoder
  val encodeBar5: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz5: Decoder[Baz] = deriveDecoder
  val encodeBaz5: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo6: Decoder[Foo] = deriveDecoder
  val encodeFoo6: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar6: Decoder[Bar] = deriveDecoder
  val encodeBar6: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz6: Decoder[Baz] = deriveDecoder
  val encodeBaz6: Encoder.AsObject[Baz] = deriveEncoder

  val decodeFoo7: Decoder[Foo] = deriveDecoder
  val encodeFoo7: Encoder.AsObject[Foo] = deriveEncoder
  val decodeBar7: Decoder[Bar] = deriveDecoder
  val encodeBar7: Encoder.AsObject[Bar] = deriveEncoder
  val decodeBaz7: Decoder[Baz] = deriveDecoder
  val encodeBaz7: Encoder.AsObject[Baz] = deriveEncoder
}
