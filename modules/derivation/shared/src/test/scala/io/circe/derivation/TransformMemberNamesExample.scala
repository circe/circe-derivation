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

package io.circe.derivation

import cats.kernel.Eq
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import org.scalacheck.Arbitrary

object TransformMemberNamesExample {
  case class User(firstName: String, lastName: String, role: Role, address: Address)

  object User {
    implicit val arbitraryUser: Arbitrary[User] = Arbitrary(
      for {
        f <- Arbitrary.arbitrary[String]
        l <- Arbitrary.arbitrary[String]
        r <- Arbitrary.arbitrary[Role]
        a <- Arbitrary.arbitrary[Address]
      } yield User(f, l, r, a)
    )

    implicit val eqUser: Eq[User] = Eq.fromUniversalEquals

    implicit val encodeUser: Encoder[User] = deriveEncoder(renaming.snakeCase, None)
    implicit val decodeUser: Decoder[User] = deriveDecoder(renaming.snakeCase, true, None)
    val codecForUser: Codec[User] = deriveCodec(renaming.snakeCase, true, None)
  }

  case class Role(title: String)

  object Role {
    implicit val arbitraryRole: Arbitrary[Role] = Arbitrary(Arbitrary.arbitrary[String].map(Role(_)))
    implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals

    implicit val encodeRole: Encoder[Role] = deriveEncoder(_.toUpperCase, None)
    implicit val decodeRole: Decoder[Role] = deriveDecoder(_.toUpperCase, true, None)
  }

  case class Address(number: Int, street: String, city: String)

  object Address {
    implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary(
      for {
        n <- Arbitrary.arbitrary[Int]
        c <- Arbitrary.arbitrary[String]
        s <- Arbitrary.arbitrary[String]
      } yield Address(n, c, s)
    )

    implicit val eqAddress: Eq[Address] = Eq.fromUniversalEquals

    implicit val encodeAddress: Encoder[Address] =
      deriveEncoder(renaming.replaceWith("number" -> "#"), None)
    implicit val decodeAddress: Decoder[Address] =
      deriveDecoder(renaming.replaceWith("number" -> "#"), true, None)
  }

  case class Abc(a: String, b: String, c: String)

  object Abc {
    implicit val arbitraryAbc: Arbitrary[Abc] = Arbitrary(
      for {
        a <- Arbitrary.arbitrary[String]
        b <- Arbitrary.arbitrary[String]
        c <- Arbitrary.arbitrary[String]
      } yield Abc(a, b, c)
    )

    implicit val eqAbc: Eq[Abc] = Eq.fromUniversalEquals

    implicit val encodeAbc: Encoder[Abc] = deriveEncoder(_ => "x", None)
    implicit val decodeAbc: Decoder[Abc] = deriveDecoder(_ => "x", true, None)
  }
}
