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

case class CustomApplyParamNamesClass(a: Int, b: Int, c: Int)

object CustomApplyParamNamesClass {
  def apply(s: Int, s1: Int, s2: Int, s3: Int): CustomApplyParamNamesClass =
    null

  implicit val arbitraryCustomApplyParamNamesClass: Arbitrary[CustomApplyParamNamesClass] = Arbitrary(
    for {
      a <- Arbitrary.arbitrary[Int]
      b <- Arbitrary.arbitrary[Int]
      c <- Arbitrary.arbitrary[Int]
    } yield new CustomApplyParamNamesClass(a, b, c)
  )

  implicit val eqCustomApplyParamNamesClass: Eq[CustomApplyParamNamesClass] =
    Eq.fromUniversalEquals[CustomApplyParamNamesClass]
}

case class CustomApplyParamTypesClass(a: Boolean, b: Boolean, c: Boolean)

object CustomApplyParamTypesClass {
  def apply(a: Int, b: Int, c: Int): CustomApplyParamTypesClass =
    null

  implicit val arbitraryCustomApplyParamTypesClass: Arbitrary[CustomApplyParamTypesClass] = Arbitrary(
    for {
      a <- Arbitrary.arbitrary[Boolean]
      b <- Arbitrary.arbitrary[Boolean]
      c <- Arbitrary.arbitrary[Boolean]
    } yield new CustomApplyParamTypesClass(a, b, c)
  )

  implicit val eqCustomApplyParamTypesClass: Eq[CustomApplyParamTypesClass] =
    Eq.fromUniversalEquals[CustomApplyParamTypesClass]
}
