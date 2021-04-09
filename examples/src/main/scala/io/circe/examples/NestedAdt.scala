package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.{ Arbitrary, Gen }

sealed trait NestedAdt
sealed trait NestedAdt1 extends NestedAdt
sealed trait NestedAdt2 extends NestedAdt
final case class NestedAdtFoo(i: Int, s: String = "abc") extends NestedAdt1 with NestedAdt2
case class NestedAdtBar(xs: List[Boolean]) extends NestedAdt
case object NestedAdtQux extends NestedAdt1

object NestedAdt {
  implicit val arbitraryNestedAdt: Arbitrary[NestedAdt] = Arbitrary(
    Gen.oneOf(
      for {
        i <- Arbitrary.arbitrary[Int]
        s <- Arbitrary.arbitrary[String]
      } yield NestedAdtFoo(i, s),
      Arbitrary.arbitrary[List[Boolean]].map(NestedAdtBar(_)),
      Gen.const(NestedAdtQux)
    )
  )
  implicit val eqNestedAdt: Eq[NestedAdt] = Eq.fromUniversalEquals
}
