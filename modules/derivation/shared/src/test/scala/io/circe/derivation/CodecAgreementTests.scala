/*
 * Copyright 2023 circe
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
import cats.laws._
import cats.laws.discipline._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Shrink
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

  def codecAgreement(implicit
    arbitraryA: Arbitrary[A],
    shrinkA: Shrink[A],
    eqA: Eq[A],
    arbitraryJson: Arbitrary[Json],
    shrinkJson: Shrink[Json]
  ): RuleSet = new DefaultRuleSet(
    name = "codec-agreement",
    parent = None,
    "agreement" -> Prop.forAll((a: A) => laws.codecsAgree(a)),
    "new decodes old" -> Prop.forAll((a: A) => laws.newDecodesOld(a)),
    "old decodes new" -> Prop.forAll((a: A) => laws.oldDecodesNew(a))
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
