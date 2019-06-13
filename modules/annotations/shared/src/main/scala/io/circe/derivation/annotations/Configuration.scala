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

  /** Transforms the names of type members in the JSON, allowing, for example,
   *  formatting or case changes
   */
  def transformMemberNames: String => String

  protected def getA(transformMemberNames: String => String): Config

  /** Creates a configuration which produces snake cased member names */
  final def withSnakeCaseMemberNames: Config =
    getA(renaming.snakeCase)

  /** Creates a configuration which produces kebab cased member names */
  final def withKebabCaseMemberNames: Config =
    getA(renaming.kebabCase)
}

object Configuration {

  /** Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration creates *both* encoder and decoder.
   */
  final case class Codec(
    transformMemberNames: String => String
  ) extends Configuration {

    type Config = Codec

    protected final def getA(transformMemberNames: String => String) =
      Codec(transformMemberNames)
  }

  /** Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration **only** creates decoder.
   */
  final case class DecodeOnly(
    transformMemberNames: String => String
  ) extends Configuration {

    type Config = DecodeOnly

    protected final def getA(transformMemberNames: String => String) =
      DecodeOnly(transformMemberNames)
  }

  /** Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration **only** creates encoder.
   */
  final case class EncodeOnly(
    transformMemberNames: String => String
  ) extends Configuration {

    type Config = EncodeOnly

    protected final def getA(transformMemberNames: String => String) =
      EncodeOnly(transformMemberNames)
  }

  /** Create a default configuration with both decoder and encoder */
  val default: Codec =
    Codec(identity)

  /** Create a default configuration with **only** encoder */
  val encodeOnly: EncodeOnly =
    EncodeOnly(identity)

  /** Create a default configuration with **only** decoder */
  val decodeOnly: DecodeOnly =
    DecodeOnly(identity)
}
