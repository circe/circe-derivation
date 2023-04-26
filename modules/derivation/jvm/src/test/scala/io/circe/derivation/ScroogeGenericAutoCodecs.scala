/*
 * Copyright 2023 circe
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

import com.stripe.scrooge.shapes._ // scalafix:ok
import io.circe.Decoder
import io.circe.Encoder
import io.circe.examples.scrooge._
import io.circe.generic.semiauto.{ deriveDecoder => genericDeriveDecoder }
import io.circe.generic.semiauto.{ deriveEncoder => genericDeriveEncoder }

object ScroogeGenericAutoCodecs {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = genericDeriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = genericDeriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = genericDeriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = genericDeriveEncoder
}
