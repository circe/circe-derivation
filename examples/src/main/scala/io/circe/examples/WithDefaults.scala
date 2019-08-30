package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class WithDefaults(i: Int, j: Int = 1, k: List[String] = List(""))

object WithDefaults {
  implicit val arbitraryWithDefaults: Arbitrary[WithDefaults] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      j <- Arbitrary.arbitrary[Int]
      k <- Arbitrary.arbitrary[List[String]]
    } yield WithDefaults(i, j, k)
  )

  implicit val eqWithDefaults: Eq[WithDefaults] = Eq.fromUniversalEquals
}