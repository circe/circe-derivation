package io.circe.derivation.annotations

import cats.instances.AllInstances
import cats.kernel.Eq
import io.circe.{Decoder, Encoder, ObjectEncoder}
import io.circe.derivation.CirceSuite
import io.circe.testing.{ ArbitraryInstances, CodecTests }
import org.scalacheck.{ Arbitrary, Gen }

package object jsoncodecmacrossuiteaux extends AnyRef
  with AllInstances with ArbitraryInstances

package jsoncodecmacrossuiteaux {

  // Simple

  @JsonCodec final case class Simple(i: Int, l: Long, s: String)

  object Simple {
    implicit def eqSimple: Eq[Simple] = Eq.by(s => (s.i, s.l, s.s))

    implicit def arbitrarySimple: Arbitrary[Simple] =
      Arbitrary(
        for {
          i <- Arbitrary.arbitrary[Int]
          l <- Arbitrary.arbitrary[Long]
          s <- Arbitrary.arbitrary[String]
        } yield Simple(i, l, s)
      )
  }

  // Single

  @JsonCodec final case class Single(i: Int)

  object Single {
    implicit def eqSingle: Eq[Single] = Eq.by(_.i)

    implicit def arbitrarySingle: Arbitrary[Single] =
      Arbitrary(Arbitrary.arbitrary[Int].map(Single(_)))
  }

  // Typed1

  @JsonCodec final case class Typed1[A](i: Int, a: A, j: Int)

  object Typed1 {
    implicit def eqTyped1[A: Eq]: Eq[Typed1[A]] = Eq.by(t => (t.i, t.a, t.j))

    implicit def arbitraryTyped1[A](implicit A: Arbitrary[A]): Arbitrary[Typed1[A]] =
      Arbitrary(
        for {
          i <- Arbitrary.arbitrary[Int]
          a <- A.arbitrary
          j <- Arbitrary.arbitrary[Int]
        } yield Typed1(i, a, j)
      )
  }

  // Typed2

  @JsonCodec final case class Typed2[A, B](i: Int, a: A, b: B, j: Int)

  object Typed2 {
    implicit def eqTyped2[A: Eq, B: Eq]: Eq[Typed2[A, B]] = Eq.by(t => (t.i, t.a, t.b, t.j))

    implicit def arbitraryTyped2[A, B](implicit A: Arbitrary[A], B: Arbitrary[B])
    : Arbitrary[Typed2[A, B]] =
      Arbitrary(
        for {
          i <- Arbitrary.arbitrary[Int]
          a <- A.arbitrary
          b <- B.arbitrary
          j <- Arbitrary.arbitrary[Int]
        } yield Typed2(i, a, b, j)
      )
  }

  // Hierarchy

//  @JsonCodec sealed trait Hierarchy
//  final case class Hierarchy1(i: Int, s: String) extends Hierarchy
//  final case class Hierarchy2(xs: List[String]) extends Hierarchy
//  final case class Hierarchy3(s: Single, d: Double) extends Hierarchy
//
//  object Hierarchy {
//    implicit val eqHierarchy: Eq[Hierarchy] = Eq.fromUniversalEquals
//
//    implicit val arbitraryHierarchy: Arbitrary[Hierarchy] = Arbitrary(
//      Gen.oneOf(
//        for {
//          i <- Arbitrary.arbitrary[Int]
//          s <- Arbitrary.arbitrary[String]
//        } yield Hierarchy1(i, s),
//        Gen.listOf(Arbitrary.arbitrary[String]).map(Hierarchy2.apply),
//        for {
//          s <- Arbitrary.arbitrary[Single]
//          d <- Arbitrary.arbitrary[Double]
//        } yield Hierarchy3(s, d)
//      )
//    )
//  }

  // SelfRecursiveWithOption

  @JsonCodec final case class SelfRecursiveWithOption(o: Option[SelfRecursiveWithOption])

  object SelfRecursiveWithOption {
    implicit val eqSelfRecursiveWithOption: Eq[SelfRecursiveWithOption] = Eq.fromUniversalEquals

    private def atDepth(depth: Int): Gen[SelfRecursiveWithOption] = if (depth < 3)
      Arbitrary.arbitrary[Option[SelfRecursiveWithOption]].map(
        SelfRecursiveWithOption(_)
      ) else Gen.const(SelfRecursiveWithOption(None))

    implicit val arbitrarySelfRecursiveWithOption: Arbitrary[SelfRecursiveWithOption] =
      Arbitrary(atDepth(0))
  }
}

class JsonCodecMacrosSuite extends CirceSuite {
  import jsoncodecmacrossuiteaux._

  checkLaws("Codec[Simple]", CodecTests[Simple].codec)
  checkLaws("Codec[Single]", CodecTests[Single].codec)
  checkLaws("Codec[Typed1[Int]]", CodecTests[Typed1[Int]].codec)
  checkLaws("Codec[Typed2[Int, Long]]", CodecTests[Typed2[Int, Long]].codec)
//  checkLaws("Codec[Hierarchy]", CodecTests[Hierarchy].codec)
  checkLaws("Codec[SelfRecursiveWithOption]", CodecTests[SelfRecursiveWithOption].codec)

  "@JsonCodec" should "provide ObjectEncoder instances" in {
    ObjectEncoder[Simple]
    ObjectEncoder[Single]
    ObjectEncoder[Typed1[Int]]
    ObjectEncoder[Typed2[Int, Long]]
//    ObjectEncoder[Hierarchy]
    ObjectEncoder[SelfRecursiveWithOption]
  }

  "@JsonCodec(config = Configuration.default)" should "create both encoder and decoder" in {
    @JsonCodec case class CaseClass1(fooCamel: String, barCamel: Int)
    ObjectEncoder[CaseClass1]
    Decoder[CaseClass1]
    Encoder[CaseClass1]

    @JsonCodec(Configuration.default) case class CaseClass2(fooCamel: String, barCamel: Int)
    ObjectEncoder[CaseClass2]
    Decoder[CaseClass2]
    Encoder[CaseClass2]

    @JsonCodec(config = Configuration.default) case class CaseClass3(fooCamel: String, barCamel: Int)
    ObjectEncoder[CaseClass3]
    Decoder[CaseClass3]
    Encoder[CaseClass3]
  }

  it should "generate the correct JSON" in {
    @JsonCodec(config = Configuration.default)
    case class CaseClass(fooCamel: String, barCamel: Int)

    val expectedJson = """{"fooCamel":"foo","barCamel":1}"""
    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces

    assertEq(expectedJson, generatedJson)
  }

  it should "generate snake case JSON" in {
    @JsonCodec(config = Configuration.default.withSnakeCaseMemberNames)
    case class CaseClass(fooSnake: String, barSnake: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo_snake":"foo","bar_snake":1}"""

    assertEq(expectedJson, generatedJson)
  }

  it should "generate kebab case JSON" in {
    @JsonCodec(config = Configuration.default.withKebabCaseMemberNames)
    case class CaseClass(fooKebab: String, barKebab: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo-kebab":"foo","bar-kebab":1}"""

    assertEq(expectedJson, generatedJson)
  }

  "@JsonCodec(config = Configuration.decodeOnly)" should "provide Decoder instances" in {
    @JsonCodec(config = Configuration.decodeOnly)
    case class CaseClassDecodeOnly(foo: String, bar: Int)

    Decoder[CaseClassDecodeOnly]
    assertDoesNotCompile("Encoder[CaseClassDecodeOnly]")
  }

  "@JsonCodec(config = Configuration.encodeOnly)" should "provide Encoder instances" in {
    @JsonCodec(config = Configuration.encodeOnly)
    case class CaseClassEncodeOnly(foo: String, bar: Int)

    Encoder[CaseClassEncodeOnly]
    assertDoesNotCompile("Decoder[CaseClassEncodeOnly]")
  }
}
