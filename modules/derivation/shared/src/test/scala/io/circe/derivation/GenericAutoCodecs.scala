/*
 * Copyright 2017 circe
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

import io.circe.Decoder
import io.circe.Encoder
import io.circe.examples._
import io.circe.generic.semiauto.{ deriveDecoder => genericDeriveDecoder }
import io.circe.generic.semiauto.{ deriveEncoder => genericDeriveEncoder }

object GenericAutoCodecs {
  implicit val decodeFoo: Decoder[Foo] = genericDeriveDecoder
  implicit val encodeFoo: Encoder.AsObject[Foo] = genericDeriveEncoder
  implicit val decodeBar: Decoder[Bar] = genericDeriveDecoder
  implicit val encodeBar: Encoder.AsObject[Bar] = genericDeriveEncoder
  implicit val decodeBaz: Decoder[Baz] = genericDeriveDecoder
  implicit val encodeBaz: Encoder.AsObject[Baz] = genericDeriveEncoder
  implicit def decodeQux[A: Decoder]: Decoder[Qux[A]] = genericDeriveDecoder
  implicit def encodeQux[A: Encoder]: Encoder.AsObject[Qux[A]] = genericDeriveEncoder
  implicit val decodeAdt: Decoder[Adt] = genericDeriveDecoder
  implicit val encodeAdt: Encoder.AsObject[Adt] = genericDeriveEncoder
  implicit val decodeNestedAdt: Decoder[NestedAdt] = genericDeriveDecoder
  implicit val encodeNestedAdt: Encoder.AsObject[NestedAdt] = genericDeriveEncoder

  implicit val decodeSimpleClass: Decoder[SimpleClass] = genericDeriveDecoder
  implicit val encodeSimpleClass: Encoder[SimpleClass] = genericDeriveEncoder
  implicit val decodeCustomApplyParamNamesClass: Decoder[CustomApplyParamNamesClass] = deriveDecoder
  implicit val encodeCustomApplyParamNamesClass: Encoder[CustomApplyParamNamesClass] = deriveEncoder
  implicit val decodeCustomApplyParamTypesClass: Decoder[CustomApplyParamTypesClass] = deriveDecoder
  implicit val encodeCustomApplyParamTypesClass: Encoder[CustomApplyParamTypesClass] = deriveEncoder
}
