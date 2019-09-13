package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

object JsonCodecADTSpecSamples {

  @JsonCodec
  sealed trait ADT1

  @JsonCodec case class ADT1A(a: Int) extends ADT1
  @JsonCodec case class ADT1B(b: Int) extends ADT1

  @JsonCodec(Configuration.default.withDiscriminator("_type"))
  sealed trait ADT1Custom

  @JsonCodec case class ADT1CustomA(a: Int) extends ADT1Custom
  @JsonCodec case class ADT1CustomB(b: Int) extends ADT1Custom

  @JsonCodec(Configuration.default)
  sealed trait ADTTyped

  @JsonCodec case class ADTTypedA(a: Int) extends ADTTyped
  @JsonCodec case class ADTTypedB(b: Int) extends ADTTyped

  // TODO: Add test cases for constructor transformations
  @JsonCodec(Configuration.default.withKebabCaseConstructorNames)
  sealed trait ADTTransformed

  @JsonCodec case class ADTTransformed1(a: Int) extends ADTTransformed
  @JsonCodec case class ADTTransformed2(b: Int) extends ADTTransformed

  @JsonCodec(Configuration.default.withSnakeCaseConstructorNames.withDiscriminator("_type"))
  sealed trait ADTSnakeDiscriminator

  @JsonCodec case class ADTSnakeDiscriminatorA(a: Int) extends ADTSnakeDiscriminator
  @JsonCodec case class ADTSnakeDiscriminatorB(b: Int) extends ADTSnakeDiscriminator
}

class JsonCodecADTSpec extends AnyWordSpec with Matchers {

  import JsonCodecADTSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  "JsonCodecADTSpec" should {

    "serialize default" in {
      val a1: ADT1 = ADT1A(1)

      a1.asJson.printWith(printer) should be("""{"ADT1A":{"a":1}}""")
      parse("""{"ADT1A":{"a":1}}""").flatMap(_.as[ADT1]) should be(
        Right(a1)
      )

      val b1: ADT1 = ADT1B(1)

      b1.asJson.printWith(printer) should be("""{"ADT1B":{"b":1}}""")
      parse("""{"ADT1B":{"b":1}}""").flatMap(_.as[ADT1]) should be(
        Right(b1)
      )
    }

    "serialize discriminator custom fieldname" in {
      val a1: ADT1Custom = ADT1CustomA(1)

      a1.asJson.printWith(printer) should be("""{"a":1,"_type":"ADT1CustomA"}""")
      parse("""{"a":1,"_type":"ADT1CustomA"}""").flatMap(_.as[ADT1Custom]) should be(Right(a1))

      val b1: ADT1Custom = ADT1CustomB(1)

      b1.asJson.printWith(printer) should be("""{"b":1,"_type":"ADT1CustomB"}""")
      parse("""{"b":1,"_type":"ADT1CustomB"}""").flatMap(_.as[ADT1Custom]) should be(Right(b1))
    }

    "serialize discriminator typed" in {
      val a1: ADTTyped = ADTTypedA(1)

      a1.asJson.printWith(printer) should be("""{"ADTTypedA":{"a":1}}""")
      parse("""{"ADTTypedA":{"a":1}}""").flatMap(_.as[ADTTyped]) should be(
        Right(a1)
      )

      val b1: ADTTyped = ADTTypedB(1)

      b1.asJson.printWith(printer) should be("""{"ADTTypedB":{"b":1}}""")
      parse("""{"ADTTypedB":{"b":1}}""").flatMap(_.as[ADTTyped]) should be(
        Right(b1)
      )
    }

    "transform constructor names" in {
      val a1: ADTTransformed = ADTTransformed1(1)

      a1.asJson.pretty(printer) should be("""{"adt-transformed1":{"a":1}}""")
      parse("""{"adt-transformed1":{"a":1}}""").right.get.as[ADTTransformed] should be(Right(a1))

      val b1: ADTTransformed = ADTTransformed2(1)

      b1.asJson.pretty(printer) should be("""{"adt-transformed2":{"b":1}}""")
      parse("""{"adt-transformed2":{"b":1}}""").right.get.as[ADTTransformed] should be(Right(b1))
    }

    "transform constructor names with a discriminator" in {
      val a1: ADTSnakeDiscriminator = ADTSnakeDiscriminatorA(1)

      a1.asJson.pretty(printer) should be("""{"a":1,"_type":"adt_snake_discriminator_a"}""")
      parse("""{"a":1,"_type":"adt_snake_discriminator_a"}""").right.get.as[ADTSnakeDiscriminator] should be(Right(a1))

      val b1: ADTSnakeDiscriminator = ADTSnakeDiscriminatorB(1)

      b1.asJson.pretty(printer) should be("""{"b":1,"_type":"adt_snake_discriminator_b"}""")
      parse("""{"b":1,"_type":"adt_snake_discriminator_b"}""").right.get.as[ADTSnakeDiscriminator] should be(Right(b1))
    }
  }
}
