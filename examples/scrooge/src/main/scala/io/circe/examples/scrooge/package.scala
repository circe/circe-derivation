/*
 * Copyright 2017 circe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
