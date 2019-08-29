package io.circe.derivation

//Used to manage different ways of ADT serialization

sealed trait Discriminator

object Discriminator {
  final case class Embedded(fieldName: String) extends Discriminator
  final case object TypeDiscriminator extends Discriminator

  val default: Discriminator = Embedded("type")
}
