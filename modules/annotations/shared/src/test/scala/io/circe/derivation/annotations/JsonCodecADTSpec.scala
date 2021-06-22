package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._

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

  @JsonCodec(Configuration.default.withKebabCaseConstructorNames)
  sealed trait ADTTransformed

  @JsonCodec case class ADTTransformed1(a: Int) extends ADTTransformed
  @JsonCodec case class ADTTransformed2(b: Int) extends ADTTransformed

  @JsonCodec(Configuration.default.withSnakeCaseConstructorNames.withDiscriminator("_type"))
  sealed trait ADTSnakeDiscriminator

  @JsonCodec case class ADTSnakeDiscriminatorA(a: Int) extends ADTSnakeDiscriminator
  @JsonCodec case class ADTSnakeDiscriminatorB(b: Int) extends ADTSnakeDiscriminator
}

class JsonCodecADTSpec extends munit.FunSuite {

  import JsonCodecADTSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  test("JsonCodecADTSpec should serialize default") {
    val a1: ADT1 = ADT1A(1)

    assertEquals(a1.asJson.printWith(printer), """{"ADT1A":{"a":1}}""")
    assertEquals(parse("""{"ADT1A":{"a":1}}""").flatMap(_.as[ADT1]), Right(a1))

    val b1: ADT1 = ADT1B(1)

    assertEquals(b1.asJson.printWith(printer), """{"ADT1B":{"b":1}}""")
    assertEquals(parse("""{"ADT1B":{"b":1}}""").flatMap(_.as[ADT1]), Right(b1))
  }

  test("serialize discriminator custom fieldname") {
    val a1: ADT1Custom = ADT1CustomA(1)

    assertEquals(a1.asJson.printWith(printer), """{"a":1,"_type":"ADT1CustomA"}""")
    assertEquals(parse("""{"a":1,"_type":"ADT1CustomA"}""").flatMap(_.as[ADT1Custom]), Right(a1))

    val b1: ADT1Custom = ADT1CustomB(1)

    assertEquals(b1.asJson.printWith(printer), """{"b":1,"_type":"ADT1CustomB"}""")
    assertEquals(parse("""{"b":1,"_type":"ADT1CustomB"}""").flatMap(_.as[ADT1Custom]), Right(b1))
  }

  test("serialize discriminator typed") {
    val a1: ADTTyped = ADTTypedA(1)

    assertEquals(a1.asJson.printWith(printer), """{"ADTTypedA":{"a":1}}""")
    assertEquals(parse("""{"ADTTypedA":{"a":1}}""").flatMap(_.as[ADTTyped]), Right(a1))

    val b1: ADTTyped = ADTTypedB(1)

    assertEquals(b1.asJson.printWith(printer), """{"ADTTypedB":{"b":1}}""")
    assertEquals(parse("""{"ADTTypedB":{"b":1}}""").flatMap(_.as[ADTTyped]), Right(b1))
  }

  test("transform constructor names") {
    val a1: ADTTransformed = ADTTransformed1(1)

    assertEquals(a1.asJson.printWith(printer), """{"adt-transformed1":{"a":1}}""")
    assertEquals(parse("""{"adt-transformed1":{"a":1}}""").right.get.as[ADTTransformed], Right(a1))

    val b1: ADTTransformed = ADTTransformed2(1)

    assertEquals(b1.asJson.printWith(printer), """{"adt-transformed2":{"b":1}}""")
    assertEquals(parse("""{"adt-transformed2":{"b":1}}""").right.get.as[ADTTransformed], Right(b1))
  }

  test("transform constructor names with a discriminator") {
    val a1: ADTSnakeDiscriminator = ADTSnakeDiscriminatorA(1)

    assertEquals(a1.asJson.printWith(printer), """{"a":1,"_type":"adt_snake_discriminator_a"}""")
    assertEquals(
      parse("""{"a":1,"_type":"adt_snake_discriminator_a"}""").right.get.as[ADTSnakeDiscriminator],
      Right(a1)
    )

    val b1: ADTSnakeDiscriminator = ADTSnakeDiscriminatorB(1)

    assertEquals(b1.asJson.printWith(printer), """{"b":1,"_type":"adt_snake_discriminator_b"}""")
    assertEquals(
      parse("""{"b":1,"_type":"adt_snake_discriminator_b"}""").right.get.as[ADTSnakeDiscriminator],
      Right(b1)
    )
  }
}
