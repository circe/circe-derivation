package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

package object scrooge {
  implicit val arbitrarySomethingStruct: Arbitrary[SomethingStruct] = Arbitrary(
    for {
      a <- Arbitrary.arbitrary[String]
      b <- Arbitrary.arbitrary[Option[Long]]
      c <- Arbitrary.arbitrary[List[String]]
    } yield SomethingStruct(a, b, c)
  )
  implicit val eqSomethingStruct: Eq[SomethingStruct] = Eq.fromUniversalEquals

  implicit val arbitraryBiggerStruct: Arbitrary[BiggerStruct] = Arbitrary(
    for {
      d <- Arbitrary.arbitrary[SomethingStruct]
      e <- Arbitrary.arbitrary[Option[String]]
    } yield BiggerStruct(d, e)
  )
  implicit val eqBiggerStruct: Eq[BiggerStruct] = Eq.fromUniversalEquals
}
