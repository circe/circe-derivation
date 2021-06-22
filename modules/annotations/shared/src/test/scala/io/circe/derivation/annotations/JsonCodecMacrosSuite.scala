package io.circe.derivation.annotations

import cats.instances.AllInstances
import cats.kernel.Eq
import io.circe.{ Decoder, Encoder }
import io.circe.derivation.CirceSuite
import io.circe.testing.{ ArbitraryInstances, CodecTests }
import org.scalacheck.{ Arbitrary, Gen }
import munit.DisciplineSuite

package object jsoncodecmacrossuiteaux extends AnyRef with AllInstances with ArbitraryInstances

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

    implicit def arbitraryTyped2[A, B](implicit A: Arbitrary[A], B: Arbitrary[B]): Arbitrary[Typed2[A, B]] =
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

  @JsonCodec
  sealed trait Hierarchy
  @JsonCodec final case class Hierarchy1(i: Int, s: String) extends Hierarchy
  @JsonCodec final case class Hierarchy2(xs: List[String]) extends Hierarchy
  @JsonCodec final case class Hierarchy3(s: Single, d: Double) extends Hierarchy

  object Hierarchy {
    implicit val eqHierarchy: Eq[Hierarchy] = Eq.fromUniversalEquals

    implicit val arbitraryHierarchy: Arbitrary[Hierarchy] = Arbitrary(
      Gen.oneOf(
        for {
          i <- Arbitrary.arbitrary[Int]
          s <- Arbitrary.arbitrary[String]
        } yield Hierarchy1(i, s),
        Gen.listOf(Arbitrary.arbitrary[String]).map(Hierarchy2.apply),
        for {
          s <- Arbitrary.arbitrary[Single]
          d <- Arbitrary.arbitrary[Double]
        } yield Hierarchy3(s, d)
      )
    )
  }

  // SelfRecursiveWithOption

  @JsonCodec final case class SelfRecursiveWithOption(o: Option[SelfRecursiveWithOption])

  object SelfRecursiveWithOption {
    implicit val eqSelfRecursiveWithOption: Eq[SelfRecursiveWithOption] = Eq.fromUniversalEquals

    private def atDepth(depth: Int): Gen[SelfRecursiveWithOption] = if (depth < 3)
      Gen.oneOf(
        Gen.const(SelfRecursiveWithOption(None)),
        atDepth(depth + 1).map(x => SelfRecursiveWithOption(Some(x)))
      )
    else Gen.const(SelfRecursiveWithOption(None))

    implicit val arbitrarySelfRecursiveWithOption: Arbitrary[SelfRecursiveWithOption] =
      Arbitrary(atDepth(0))
  }
}

class JsonCodecMacrosSuite extends CirceSuite with DisciplineSuite {
  import jsoncodecmacrossuiteaux._

  checkAll("Codec[Simple]", CodecTests[Simple].codec)
  checkAll("Codec[Single]", CodecTests[Single].codec)
  checkAll("Codec[Typed1[Int]]", CodecTests[Typed1[Int]].codec)
  checkAll("Codec[Typed2[Int, Long]]", CodecTests[Typed2[Int, Long]].codec)
  checkAll("Codec[Hierarchy]", CodecTests[Hierarchy].codec)
  checkAll(
    "Codec[SelfRecursiveWithOption]",
    CodecTests[SelfRecursiveWithOption].codec
  )

  test("@JsonCodec should provide Encoder.AsObject instances") {
    Encoder.AsObject[Simple]
    Encoder.AsObject[Single]
    Encoder.AsObject[Typed1[Int]]
    Encoder.AsObject[Typed2[Int, Long]]
    // Encoder.AsObject[Hierarchy]
    Encoder.AsObject[SelfRecursiveWithOption]
  }

  test("@JsonCodec(config = Configuration.default) should create both encoder and decoder") {
    @JsonCodec case class CaseClass1(fooCamel: String, barCamel: Int)
    Encoder.AsObject[CaseClass1]
    Decoder[CaseClass1]
    Encoder[CaseClass1]

    @JsonCodec(Configuration.default) case class CaseClass2(fooCamel: String, barCamel: Int)
    Encoder.AsObject[CaseClass2]
    Decoder[CaseClass2]
    Encoder[CaseClass2]

    @JsonCodec(config = Configuration.default) case class CaseClass3(fooCamel: String, barCamel: Int)
    Encoder.AsObject[CaseClass3]
    Decoder[CaseClass3]
    Encoder[CaseClass3]
  }

  test("generate the correct JSON") {
    @JsonCodec(config = Configuration.default)
    case class CaseClass(fooCamel: String, barCamel: Int)

    val expectedJson = """{"fooCamel":"foo","barCamel":1}"""
    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces

    assertEquals(expectedJson, generatedJson)
  }

  test("generate snake case JSON") {
    @JsonCodec(config = Configuration.default.withSnakeCaseMemberNames)
    case class CaseClass(fooSnake: String, barSnake: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo_snake":"foo","bar_snake":1}"""

    assertEquals(expectedJson, generatedJson)
  }

  test("generate kebab case JSON") {
    @JsonCodec(config = Configuration.default.withKebabCaseMemberNames)
    case class CaseClass(fooKebab: String, barKebab: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo-kebab":"foo","bar-kebab":1}"""

    assertEquals(expectedJson, generatedJson)
  }

  test("@JsonCodec(config = Configuration.decodeOnly) should provide Decoder instances") {
    @JsonCodec(config = Configuration.decodeOnly)
    case class CaseClassDecodeOnly(foo: String, bar: Int)

    Decoder[CaseClassDecodeOnly]
    assert(compileErrors("Encoder[CaseClassDecodeOnly]").nonEmpty)
  }

  test("@JsonCodec(config = Configuration.encodeOnly) should provide Encoder instances") {
    @JsonCodec(config = Configuration.encodeOnly)
    case class CaseClassEncodeOnly(foo: String, bar: Int)

    Encoder[CaseClassEncodeOnly]
    assert(compileErrors("Decoder[CaseClassEncodeOnly]").nonEmpty)
  }

  test("@SnakeCaseJsonCodec should generate snake case JSON") {
    @SnakeCaseJsonCodec case class CaseClass(fooSnake: String, barSnake: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo_snake":"foo","bar_snake":1}"""

    assertEquals(expectedJson, generatedJson)
  }

  test("@KebabCaseJsonCodec should generate kebab case JSON") {
    @KebabCaseJsonCodec case class CaseClass(fooKebab: String, barKebab: Int)

    val generatedJson = Encoder[CaseClass].apply(CaseClass("foo", 1)).noSpaces
    val expectedJson = """{"foo-kebab":"foo","bar-kebab":1}"""

    assertEquals(expectedJson, generatedJson)
  }
}
