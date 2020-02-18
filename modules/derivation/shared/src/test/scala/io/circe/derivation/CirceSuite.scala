package io.circe.derivation

import cats.Eq
import cats.instances.AllInstances
import cats.syntax.{ AllSyntax, EitherOps }
import io.circe.testing.{ ArbitraryInstances, EqInstances }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.discipline.scalatest.FlatSpecDiscipline
import scala.language.implicitConversions

/**
 * An opinionated stack of traits to improve consistency and reduce boilerplate in circe tests.
 */
trait CirceSuite
    extends AnyFlatSpec
    with FlatSpecDiscipline
    with ScalaCheckDrivenPropertyChecks
    with AllInstances
    with AllSyntax
    with ArbitraryInstances
    with EqInstances {
  override def convertToEqualizer[T](left: T): Equalizer[T] =
    sys.error("Intentionally ambiguous implicit for Equalizer")

  implicit def prioritizedCatsSyntaxEither[A, B](eab: Either[A, B]): EitherOps[A, B] = new EitherOps(eab)

  def assertEq[A: Eq](expected: A, actual: A): Unit =
    assert(
      expected === actual,
      s"""Expected did not match actual:
         |
         |Expected: $expected
         |Actual:   $actual"""
    )
}
