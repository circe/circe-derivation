package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class WithoutDefaults(i: Int, j: Int = 1, k: Option[Int] = Some(5))

object WithoutDefaults {
  implicit val arbitraryWithoutDefaults: Arbitrary[WithoutDefaults] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      j <- Arbitrary.arbitrary[Int]
      k <- Arbitrary.arbitrary[Option[Int]]
    } yield WithoutDefaults(i, j, k)
  )

  implicit val eqWithoutDefaults: Eq[WithoutDefaults] = Eq.fromUniversalEquals
}
