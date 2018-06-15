package io.circe.derivation

/**
  * Configuration allowing customisation of the JSON produced when encoding, or
  * expected when decoding. Can be passed to the [[JsonCodec]] annotation to
  * allow customisation of derivation.
  *
  * The configuration also determines if either *both* encoder and decoder are
  * derived - or if only one of them will be.
  */
sealed trait Configuration {

  type A <: Configuration

  /** Transforms the names of type members in the JSON, allowing, for example,
   *  formatting or case changes
   */
  def transformMemberNames: String => String

  protected def getA(transformMemberNames: String => String): A

  /** Creates a configuration which produces snake cased member names */
  final def withSnakeCaseMemberNames: A =
    getA(renaming.snakeCase)

  /** Creates a configuration which produces kebab cased member names */
  final def withKebabCaseMemberNames: A =
    getA(renaming.kebabCase)
}

object Configuration {

  /** Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration creates *both* encoder and decoder.
   */
  final case class Both(
    transformMemberNames: String => String
  ) extends Configuration {

    type A = Both

    protected final def getA(transformMemberNames: String => String) =
      Both(transformMemberNames)
  }

  /** Configuration allowing customisation of JSON produced when encoding or
   *  decoding.
   *
   *  This configuration **only** creates decoder.
   */
  final case class DecodeOnly(
    transformMemberNames: String => String,
  ) extends Configuration {

    type A = DecodeOnly

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

    type A = EncodeOnly

    protected final def getA(transformMemberNames: String => String) =
      EncodeOnly(transformMemberNames)
  }

  /** Create a default configuration with both decoder and encoder */
  val default: Both =
    Both(identity)

  /** Create a default configuration with **only** encoder */
  val encodeOnly: EncodeOnly =
    EncodeOnly(identity)

  /** Create a default configuration with **only** decoder */
  val decodeOnly: DecodeOnly =
    DecodeOnly(identity)
}
