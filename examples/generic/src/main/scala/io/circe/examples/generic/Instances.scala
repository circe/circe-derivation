package io.circe.examples.generic

import io.circe.{ Decoder, Encoder, ObjectEncoder }
import io.circe.examples.{ Bar, Baz, Foo, Qux }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

object Instances {
  implicit val decodeFoo0: Decoder[Foo] = deriveDecoder
  implicit val encodeFoo0: ObjectEncoder[Foo] = deriveEncoder
  implicit val decodeBar0: Decoder[Bar] = deriveDecoder
  implicit val encodeBar0: ObjectEncoder[Bar] = deriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = deriveDecoder
  implicit def encodeQux[A: Encoder]: ObjectEncoder[Qux[A]] = deriveEncoder

  val decodeBaz0: Decoder[Baz] = deriveDecoder
  val encodeBaz0: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo1: Decoder[Foo] = deriveDecoder
  val encodeFoo1: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar1: Decoder[Bar] = deriveDecoder
  val encodeBar1: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz1: Decoder[Baz] = deriveDecoder
  val encodeBaz1: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo2: Decoder[Foo] = deriveDecoder
  val encodeFoo2: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar2: Decoder[Bar] = deriveDecoder
  val encodeBar2: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz2: Decoder[Baz] = deriveDecoder
  val encodeBaz2: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo3: Decoder[Foo] = deriveDecoder
  val encodeFoo3: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar3: Decoder[Bar] = deriveDecoder
  val encodeBar3: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz3: Decoder[Baz] = deriveDecoder
  val encodeBaz3: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo4: Decoder[Foo] = deriveDecoder
  val encodeFoo4: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar4: Decoder[Bar] = deriveDecoder
  val encodeBar4: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz4: Decoder[Baz] = deriveDecoder
  val encodeBaz4: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo5: Decoder[Foo] = deriveDecoder
  val encodeFoo5: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar5: Decoder[Bar] = deriveDecoder
  val encodeBar5: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz5: Decoder[Baz] = deriveDecoder
  val encodeBaz5: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo6: Decoder[Foo] = deriveDecoder
  val encodeFoo6: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar6: Decoder[Bar] = deriveDecoder
  val encodeBar6: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz6: Decoder[Baz] = deriveDecoder
  val encodeBaz6: ObjectEncoder[Baz] = deriveEncoder

  val decodeFoo7: Decoder[Foo] = deriveDecoder
  val encodeFoo7: ObjectEncoder[Foo] = deriveEncoder
  val decodeBar7: Decoder[Bar] = deriveDecoder
  val encodeBar7: ObjectEncoder[Bar] = deriveEncoder
  val decodeBaz7: Decoder[Baz] = deriveDecoder
  val encodeBaz7: ObjectEncoder[Baz] = deriveEncoder
}
