package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class Wibble(wobble: String)

object Wibble {
  implicit def arbitraryWibble: Arbitrary[Wibble] = Arbitrary(
    for {
      s <- Arbitrary.arbitrary[String]
    } yield Wibble(s)
  )

  implicit def eqWibble: Eq[Wibble] = Eq.instance {
    case (Wibble(s1), Wibble(s2)) => s1 == s2
  }
}
