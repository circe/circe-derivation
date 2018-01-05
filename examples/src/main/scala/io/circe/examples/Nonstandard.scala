package io.circe.examples

import cats.kernel.Eq
import cats.kernel.instances.all._
import org.scalacheck.Arbitrary

class SimpleClass(val i: Int, val s: String)

object SimpleClass {
  implicit val arbitrarySimpleClass: Arbitrary[SimpleClass] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      s <- Arbitrary.arbitrary[String]
    } yield new SimpleClass(i, s)
  )

  implicit val eqSimpleClass: Eq[SimpleClass] = Eq.by[SimpleClass, (Int, String)](v => (v.i, v.s))
}

class MultiParamListClass(val i: Int, val s: String)(val cs: List[Char])

object MultiParamListClass {
  implicit val arbitraryMultiParamListClass: Arbitrary[MultiParamListClass] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      s <- Arbitrary.arbitrary[String]
      cs <- Arbitrary.arbitrary[List[Char]]
    } yield new MultiParamListClass(i, s)(cs)
  )

  implicit val eqMultiParamListClass: Eq[MultiParamListClass] =
    Eq.by[MultiParamListClass, (Int, String, List[Char])](v => (v.i, v.s, v.cs))
}