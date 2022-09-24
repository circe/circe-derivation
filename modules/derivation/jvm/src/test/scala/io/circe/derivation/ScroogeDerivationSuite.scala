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
import io.circe.examples.scrooge._
import io.circe.testing.CodecTests

object ScroogeDerivationSuiteCodecs extends Serializable {
  implicit val decodeSomethingStruct: Decoder[SomethingStruct] = deriveDecoder
  implicit val encodeSomethingStruct: Encoder.AsObject[SomethingStruct] = deriveEncoder
  val codecForSomethingStruct: Codec.AsObject[SomethingStruct] = deriveCodec

  implicit val decodeBiggerStruct: Decoder[BiggerStruct] = deriveDecoder
  implicit val encodeBiggerStruct: Encoder.AsObject[BiggerStruct] = deriveEncoder
  val codecForBiggerStruct: Codec.AsObject[BiggerStruct] = deriveCodec
}

class ScroogeDerivationSuite extends CirceSuite {
  import ScroogeDerivationSuiteCodecs._

  checkAll("Codec[SomethingStruct]", CodecTests[SomethingStruct].codec)
  checkAll(
    "Codec[SomethingStruct] via Codec",
    CodecTests[SomethingStruct](codecForSomethingStruct, codecForSomethingStruct).codec
  )
  checkAll("Codec[BiggerStruct]", CodecTests[BiggerStruct].codec)
  checkAll("Codec[BiggerStruct] via Codec", CodecTests[BiggerStruct](codecForBiggerStruct, codecForBiggerStruct).codec)

  checkAll(
    "CodecAgreement[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      ScroogeGenericAutoCodecs.decodeSomethingStruct,
      ScroogeGenericAutoCodecs.encodeSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[SomethingStruct]",
    CodecAgreementTests[SomethingStruct](
      codecForSomethingStruct,
      codecForSomethingStruct,
      decodeSomethingStruct,
      encodeSomethingStruct
    ).codecAgreement
  )

  checkAll(
    "CodecAgreementWithCodec[BiggerStruct]",
    CodecAgreementTests[BiggerStruct](
      codecForBiggerStruct,
      codecForBiggerStruct,
      decodeBiggerStruct,
      encodeBiggerStruct
    ).codecAgreement
  )
}
