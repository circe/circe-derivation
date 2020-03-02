package io.circe.examples

import cats.kernel.Eq
import io.circe.Json
import io.circe.testing.instances._
import org.scalacheck.Arbitrary

case class WithJson(i: Int, j: Int = 1, embedded: Json = Json.obj(), k: List[String] = List(""))

object WithJson {
  implicit val arbitraryWithDefaults: Arbitrary[WithJson] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      j <- Arbitrary.arbitrary[Int]
      e <- Arbitrary.arbitrary[Json]
      k <- Arbitrary.arbitrary[List[String]]
    } yield WithJson(i, j, e, k)
  )

  implicit val eqWithJson: Eq[WithJson] = Eq.fromUniversalEquals
}