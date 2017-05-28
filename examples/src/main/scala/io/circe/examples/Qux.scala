package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class Qux[A](i: Int, a: A, s: String)

object Qux {
  implicit def arbitraryQux[A](implicit arbitraryA: Arbitrary[A]): Arbitrary[Qux[A]] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      a <- Arbitrary.arbitrary[A]
      s <- Arbitrary.arbitrary[String]
    } yield Qux(i, a, s)
  )

  implicit def eqQux[A](implicit eqA: Eq[A]): Eq[Qux[A]] = Eq.instance {
    case (Qux(i1, a1, s1), Qux(i2, a2, s2)) => i1 == i2 && eqA.eqv(a1, a2) && s1 == s2
  }
}
