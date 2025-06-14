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

package io.circe.examples.derivation

import io.circe.Decoder
import io.circe.Encoder
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder
import io.circe.examples.scrooge.BiggerStruct
import io.circe.examples.scrooge.SomethingStruct

object ScroogeInstances {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = deriveEncoder
  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct1: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct1: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct1: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct1: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct2: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct2: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct2: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct2: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct3: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct3: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct3: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct3: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct4: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct4: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct4: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct4: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct5: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct5: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct5: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct5: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct6: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct6: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct6: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct6: Encoder.AsObject[BiggerStruct] = deriveEncoder

  val decodeSomethingStruct7: Decoder[SomethingStruct] = deriveDecoder
  val encodeSomethingStruct7: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val decodeBiggerStruct7: Decoder[BiggerStruct] = deriveDecoder
  val encodeBiggerStruct7: Encoder.AsObject[BiggerStruct] = deriveEncoder
}
