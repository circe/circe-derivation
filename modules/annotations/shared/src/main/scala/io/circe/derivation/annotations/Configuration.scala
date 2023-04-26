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

package io.circe.derivation.annotations

import io.circe.derivation.renaming

/**
 * Configuration allowing customisation of the JSON produced when encoding, or
 * expected when decoding. Can be passed to the [[JsonCodec]] annotation to
 * allow customisation of derivation.
 *
 * The configuration also determines if either *both* encoder and decoder are
 * derived - or if only one of them will be.
 */
sealed trait Configuration {
  type Config <: Configuration

  /**
   * Transforms the names of type members in the JSON, allowing, for example,
   *  formatting or case changes
   */
  def transformMemberNames: String => String

  /**
   * Transforms the value of any constructor names in the JSON, allowing,
   *  for example, formatting or case changes
   */
  def transformConstructorNames: String => String

  def useDefaults: Boolean

  def discriminator: Option[String]

  protected def getA(transformMemberNames: String => String): Config

  protected def getC(transformConstructorNames: String => String): Config

  protected def applyDiscriminator(discriminator: Option[String]): Config

  /** Creates a configuration which produces snake cased member names */
  final def withSnakeCaseMemberNames: Config =
    getA(renaming.snakeCase)

  /** Creates a configuration which produces kebab cased member names */
  final def withKebabCaseMemberNames: Config =
    getA(renaming.kebabCase)

  /** Creates a configuration which produces snake cased constructor names */
  final def withSnakeCaseConstructorNames: Config =
    getC(renaming.snakeCase)

  /** Creates a configuration which produces kebab cased constructor names */
  final def withKebabCaseConstructorNames: Config =
    getC(renaming.kebabCase)

  final def withDiscriminator(name: String): Config =
    applyDiscriminator(Some(name))
}

object Configuration {

  /**
   * Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration creates *both* encoder and decoder.
   */
  final case class Codec(
    transformMemberNames: String => String,
    transformConstructorNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ) extends Configuration {

    type Config = Codec

    protected final def getA(transformMemberNames: String => String) =
      Codec(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def getC(transformConstructorNames: String => String) =
      Codec(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def applyDiscriminator(
      discriminator: Option[String]
    ): Codec =
      Codec(transformMemberNames, transformConstructorNames, useDefaults, discriminator)
  }

  /**
   * Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration **only** creates decoder.
   */
  final case class DecodeOnly(
    transformMemberNames: String => String,
    transformConstructorNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ) extends Configuration {

    type Config = DecodeOnly

    protected final def getA(transformMemberNames: String => String) =
      DecodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def getC(transformConstructorNames: String => String) =
      DecodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def applyDiscriminator(discriminator: Option[String]) =
      DecodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)
  }

  /**
   * Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration **only** creates encoder.
   */
  final case class EncodeOnly(
    transformMemberNames: String => String,
    transformConstructorNames: String => String,
    useDefaults: Boolean,
    discriminator: Option[String]
  ) extends Configuration {

    type Config = EncodeOnly

    protected final def getA(transformMemberNames: String => String) =
      EncodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def getC(transformConstructorNames: String => String) =
      EncodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

    protected final def applyDiscriminator(discriminator: Option[String]) =
      EncodeOnly(transformMemberNames, transformConstructorNames, useDefaults, discriminator)

  }

  /** Create a default configuration with both decoder and encoder */
  val default: Codec =
    Codec(identity, identity, true, None)

  /** Create a default configuration with **only** encoder */
  val encodeOnly: EncodeOnly =
    EncodeOnly(identity, identity, true, None)

  /** Create a default configuration with **only** decoder */
  val decodeOnly: DecodeOnly =
    DecodeOnly(identity, identity, true, None)
}
