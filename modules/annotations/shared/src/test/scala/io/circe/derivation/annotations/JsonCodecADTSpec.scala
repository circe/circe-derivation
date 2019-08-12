package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest._

object JsonCodecADTSpecSamples {

  @JsonCodec
  sealed trait ADT1

  @JsonCodec case class ADT1A(a: Int) extends ADT1
  @JsonCodec case class ADT1B(b: Int) extends ADT1

  @JsonCodec(Configuration.default.withDiscriminatorName("_type"))
  sealed trait ADT1Custom

  @JsonCodec case class ADT1CustomA(a: Int) extends ADT1Custom
  @JsonCodec case class ADT1CustomB(b: Int) extends ADT1Custom

  @JsonCodec(Configuration.default.withTypeDiscriminator)
  sealed trait ADTTyped

  @JsonCodec case class ADTTypedA(a: Int) extends ADTTyped
  @JsonCodec case class ADTTypedB(b: Int) extends ADTTyped
}

class JsonCodecADTSpec extends WordSpec with Matchers {

  import JsonCodecADTSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  "JsonCodecADTSpec" should {

    "serialize default" in {
      val a1: ADT1 = ADT1A(1)

      a1.asJson.pretty(printer) should be("""{"a":1,"type":"adt1a"}""")
      parse("""{"a":1,"type":"adt1a"}""").right.get.as[ADT1] should be(
        Right(a1)
      )

      val b1: ADT1 = ADT1B(1)

      b1.asJson.pretty(printer) should be("""{"b":1,"type":"adt1b"}""")
      parse("""{"b":1,"type":"adt1b"}""").right.get.as[ADT1] should be(
        Right(b1)
      )
    }

    "serialize discriminator custom fieldname" in {
      val a1: ADT1Custom = ADT1CustomA(1)

      a1.asJson.pretty(printer) should be("""{"a":1,"_type":"adt1customa"}""")
      parse("""{"a":1,"_type":"adt1customa"}""").right.get.as[ADT1Custom] should be(Right(a1))

      val b1: ADT1Custom = ADT1CustomB(1)

      b1.asJson.pretty(printer) should be("""{"b":1,"_type":"adt1customb"}""")
      parse("""{"b":1,"_type":"adt1customb"}""").right.get.as[ADT1Custom] should be(Right(b1))
    }

    "serialize discriminator typed" in {
      val a1: ADTTyped = ADTTypedA(1)

      a1.asJson.pretty(printer) should be("""{"adttypeda":{"a":1}}""")
      parse("""{"adttypeda":{"a":1}}""").right.get.as[ADTTyped] should be(
        Right(a1)
      )

      val b1: ADTTyped = ADTTypedB(1)

      b1.asJson.pretty(printer) should be("""{"adttypedb":{"b":1}}""")
      parse("""{"adttypedb":{"b":1}}""").right.get.as[ADTTyped] should be(
        Right(b1)
      )
    }
  }
}
