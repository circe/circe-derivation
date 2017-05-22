package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class Foo(
  a: Int,
  b: Int,
  c: Int,
  d: Int,
  e: String,
  f: String,
  g: String,
  h: String,
  i: Option[String],
  j: Option[String],
  k: Option[String],
  l: Option[String],
  m: List[String],
  n: List[String]
)

object Foo {
  implicit val arbitraryFoo: Arbitrary[Foo] = Arbitrary(
  	for {
  	  a <- Arbitrary.arbitrary[Int]
  	  b <- Arbitrary.arbitrary[Int]
  	  c <- Arbitrary.arbitrary[Int]
  	  d <- Arbitrary.arbitrary[Int]
  	  e <- Arbitrary.arbitrary[String]
  	  f <- Arbitrary.arbitrary[String]
  	  g <- Arbitrary.arbitrary[String]
  	  h <- Arbitrary.arbitrary[String]
  	  i <- Arbitrary.arbitrary[Option[String]]
  	  j <- Arbitrary.arbitrary[Option[String]]
  	  k <- Arbitrary.arbitrary[Option[String]]
  	  l <- Arbitrary.arbitrary[Option[String]]
  	  m <- Arbitrary.arbitrary[List[String]]
  	  n <- Arbitrary.arbitrary[List[String]]
  	} yield Foo(a, b, c, d, e, f, g, h, i, j, k, l, m, n)
  )

  implicit val eqBar: Eq[Foo] = Eq.fromUniversalEquals
}
