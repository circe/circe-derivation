package io.circe.derivation

import cats.instances.AllInstances
import cats.syntax.{ AllSyntax, EitherOps }
import io.circe.testing.{ ArbitraryInstances, EqInstances }
import munit.ScalaCheckSuite
import scala.language.implicitConversions

/**
 * An opinionated stack of traits to improve consistency and reduce boilerplate in circe tests.
 */
trait CirceSuite extends ScalaCheckSuite with AllInstances with AllSyntax with ArbitraryInstances with EqInstances {
  implicit def prioritizedCatsSyntaxEither[A, B](eab: Either[A, B]): EitherOps[A, B] = new EitherOps(eab)
}
