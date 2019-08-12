package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest._

object CustomJsonCodecSpecSamples {

  @JsonCodec
  case class Person(
    @JsonKey("n") name: String,
    @JsonNoDefault @JsonKey("a") age: Int = 0,
    optional: Option[String] = None
  )

  @JsonCodec
  case class RemoveEmptyPerson(
    optional: Option[String] = None,
    values: List[String] = Nil,
    valuesSet: Set[String] = Set.empty[String],
    obj: Json = Json.obj()
  )

  @JsonCodec
  case class Group(name: String, team: Map[String, Person])

}

class CustomJsonCodecSpec extends WordSpec with Matchers {

  import CustomJsonCodecSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  "CustomJsonCodec" should {

    io.circe.Decoder
    "correct generate json" in {
      val p1 = Person("andrea")

      p1.asJson.pretty(printer) should be("{\"n\":\"andrea\"}")
      parse("""{"n":"andrea"}""").right.get.as[Person] should be(Right(p1))
    }

    "remove empty values" in {
      val p1 = RemoveEmptyPerson()

      p1.asJson.pretty(printer) should be(
        """{"values":[],"valuesSet":[],"obj":{}}"""
      )

      val p2 = RemoveEmptyPerson(values = List("a"))

      p2.asJson.pretty(printer) should be(
        """{"values":["a"],"valuesSet":[],"obj":{}}"""
      )
      parse("""{}""").right.get.as[RemoveEmptyPerson] should be(Right(p1))
    }

    "manage map" in {
      val g1 = Group("name", Map("peter" -> Person("Peter", 18)))

      g1.asJson.pretty(printer) should be(
        """{"name":"name","team":{"peter":{"n":"Peter","a":18}}}"""
      )

    }

  }
}
