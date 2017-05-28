package io.circe.examples

import cats.kernel.Eq
import org.scalacheck.Arbitrary

case class Baz(
  a: Bar,
  b: Int,
  c: Int,
  d: Int,
  e: Int,
  f: Int,
  g: Int,
  h: Int,
  i: Int,
  j: Int,
  k: Int,
  l: Int,
  m: Foo,
  n: Bar,
  o: Int,
  p: Int,
  q: Int,
  r: Int,
  s: Int,
  t: Int,
  u: Int,
  v: Int,
  w: Int,
  x: Int,
  y: Int,
  z: Foo,
  aa: Qux[Int],
  bb: Qux[Bar]
)

object Baz {
  implicit val arbitraryBaz: Arbitrary[Baz] = Arbitrary(
    for {
      a <- Arbitrary.arbitrary[Bar]
      b <- Arbitrary.arbitrary[Int]
      c <- Arbitrary.arbitrary[Int]
      d <- Arbitrary.arbitrary[Int]
      e <- Arbitrary.arbitrary[Int]
      f <- Arbitrary.arbitrary[Int]
      g <- Arbitrary.arbitrary[Int]
      h <- Arbitrary.arbitrary[Int]
      i <- Arbitrary.arbitrary[Int]
      j <- Arbitrary.arbitrary[Int]
      k <- Arbitrary.arbitrary[Int]
      l <- Arbitrary.arbitrary[Int]
      m <- Arbitrary.arbitrary[Foo]
      n <- Arbitrary.arbitrary[Bar]
      o <- Arbitrary.arbitrary[Int]
      p <- Arbitrary.arbitrary[Int]
      q <- Arbitrary.arbitrary[Int]
      r <- Arbitrary.arbitrary[Int]
      s <- Arbitrary.arbitrary[Int]
      t <- Arbitrary.arbitrary[Int]
      u <- Arbitrary.arbitrary[Int]
      v <- Arbitrary.arbitrary[Int]
      w <- Arbitrary.arbitrary[Int]
      x <- Arbitrary.arbitrary[Int]
      y <- Arbitrary.arbitrary[Int]
      z <- Arbitrary.arbitrary[Foo]
      aa <- Arbitrary.arbitrary[Qux[Int]]
      bb <- Arbitrary.arbitrary[Qux[Bar]]
    } yield Baz(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, aa, bb)
  )

  implicit val eqBaz: Eq[Baz] = Eq.fromUniversalEquals
}
