package io.circe.derivation

import cats.kernel.Eq
import io.circe.{Codec, Decoder, Encoder}
import org.scalacheck.Arbitrary

object StrictDecodingExample {
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
    implicit val decodeUser: Decoder[User] = deriveDecoder(renaming.snakeCase, true, None, true)
    val codecForUser: Codec[User] = deriveCodec(renaming.snakeCase, true, None, true)
  }

  case class Role(title: String)

  object Role {
    implicit val arbitraryRole: Arbitrary[Role] = Arbitrary(Arbitrary.arbitrary[String].map(Role(_)))
    implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals

    implicit val encodeRole: Encoder[Role] = deriveEncoder(_.toUpperCase, None)
    implicit val decodeRole: Decoder[Role] = deriveDecoder(_.toUpperCase, true, None, true)
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
      deriveDecoder(renaming.replaceWith("number" -> "#"), true, None, true)
  }
}
