package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.{ Arbitrary, Gen }

sealed trait Adt
case class AdtFoo(i: Int, s: String = "abc") extends Adt
case class AdtBar(xs: List[Boolean]) extends Adt
case object AdtQux extends Adt

object Adt {
  implicit val arbitraryAdt: Arbitrary[Adt] = Arbitrary(
    Gen.oneOf(
      for {
        i <- Arbitrary.arbitrary[Int]
        s <- Arbitrary.arbitrary[String]
      } yield AdtFoo(i, s),
      Arbitrary.arbitrary[List[Boolean]].map(AdtBar(_)),
      Gen.const(AdtQux)
    )
  )
  implicit val eqAdt: Eq[Adt] = Eq.fromUniversalEquals
}
