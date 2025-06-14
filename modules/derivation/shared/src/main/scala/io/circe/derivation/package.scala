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

package io.circe

package object derivation {
  final def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoder[A]
  final def deriveEncoder[A]: Encoder.AsObject[A] = macro DerivationMacros.materializeEncoder[A]
  final def deriveCodec[A]: Codec.AsObject[A] = macro DerivationMacros.materializeCodec[A]

  final def deriveDecoder[A](
    transformNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithTransformNames[A]

  final def deriveDecoder[A](transformNames: String => String): Decoder[A] =
    macro DerivationMacros.materializeDecoderWithTransformNamesAndDefaults[A]

  final def deriveEncoder[A](
    transformNames: String => String,
    discriminator: Option[String]
  ): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithTransformNames[A]

  final def deriveEncoder[A](transformNames: String => String): Encoder.AsObject[A] =
    macro DerivationMacros.materializeEncoderWithTransformNamesAndDefaults[A]

  final def deriveCodec[A](
    transformNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithTransformNames[A]

  final def deriveCodec[A](
    transformNames: String => String
  ): Codec.AsObject[A] =
    macro DerivationMacros.materializeCodecWithTransformNamesAndDefaults[A]
}
