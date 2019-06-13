package io.circe.derivation

import cats.instances.either._
import cats.kernel.Eq
import cats.laws._
import cats.laws.discipline._
import io.circe.{ Decoder, Encoder, Json }
import org.scalacheck.{ Arbitrary, Prop, Shrink }
import org.typelevel.discipline.Laws

trait CodecAgreementLaws[A] {
  def decodeOld: Decoder[A]
  def encodeOld: Encoder[A]
  def decodeNew: Decoder[A]
  def encodeNew: Encoder[A]

  def codecsAgree(a: A): IsEq[Json] =
    encodeNew(a) <-> encodeOld(a)

  def newDecodesOld(a: A): IsEq[Decoder.Result[A]] =
    decodeNew.decodeJson(encodeOld(a)) <-> Right(a)

  def oldDecodesNew(a: A): IsEq[Decoder.Result[A]] =
    decodeOld.decodeJson(encodeNew(a)) <-> Right(a)
}

object CodecAgreementLaws {
  def apply[A](
    decodeAOld: Decoder[A],
    encodeAOld: Encoder[A],
    decodeANew: Decoder[A],
    encodeANew: Encoder[A]
  ): CodecAgreementLaws[A] = new CodecAgreementLaws[A] {
    val decodeOld: Decoder[A] = decodeAOld
    val encodeOld: Encoder[A] = encodeAOld
    val decodeNew: Decoder[A] = decodeANew
    val encodeNew: Encoder[A] = encodeANew
  }
}

trait CodecAgreementTests[A] extends Laws {
  def laws: CodecAgreementLaws[A]

  def codecAgreement(
    implicit
    arbitraryA: Arbitrary[A],
    shrinkA: Shrink[A],
    eqA: Eq[A],
    arbitraryJson: Arbitrary[Json],
    shrinkJson: Shrink[Json]
  ): RuleSet = new DefaultRuleSet(
    name = "codec-agreement",
    parent = None,
    "agreement" -> Prop.forAll { (a: A) =>
      laws.codecsAgree(a)
    },
    "new decodes old" -> Prop.forAll { (a: A) =>
      laws.newDecodesOld(a)
    },
    "old decodes new" -> Prop.forAll { (a: A) =>
      laws.oldDecodesNew(a)
    }
  )
}

object CodecAgreementTests {
  def apply[A](
    decodeOld: Decoder[A],
    encodeOld: Encoder[A],
    decodeNew: Decoder[A],
    encodeNew: Encoder[A]
  ): CodecAgreementTests[A] = new CodecAgreementTests[A] {
    val laws: CodecAgreementLaws[A] =
      CodecAgreementLaws[A](decodeOld, encodeOld, decodeNew, encodeNew)
  }
}
