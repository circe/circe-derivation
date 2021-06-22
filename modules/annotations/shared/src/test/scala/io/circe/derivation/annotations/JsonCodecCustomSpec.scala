package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import munit.FunSuite

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

class CustomJsonCodecSpec extends FunSuite {

  import CustomJsonCodecSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  io.circe.Decoder
  test("CustomJsonCodec should correct generate json") {
    val p1 = Person("andrea")

    assertEquals(p1.asJson.printWith(printer), "{\"n\":\"andrea\"}")
    assertEquals(parse("""{"n":"andrea"}""").flatMap(_.as[Person]), Right(p1))
  }

  test("remove empty values") {
    val p1 = RemoveEmptyPerson()

    assertEquals(
      p1.asJson.printWith(printer),
      """{"values":[],"valuesSet":[],"obj":{}}"""
    )

    val p2 = RemoveEmptyPerson(values = List("a"))

    assertEquals(
      p2.asJson.printWith(printer),
      """{"values":["a"],"valuesSet":[],"obj":{}}"""
    )
    assertEquals(
      parse("""{}""").flatMap(_.as[RemoveEmptyPerson]),
      Right(p1)
    )
  }

  test("manage map") {
    val g1 = Group("name", Map("peter" -> Person("Peter", 18)))

    assertEquals(
      g1.asJson.printWith(printer),
      """{"name":"name","team":{"peter":{"n":"Peter","a":18}}}"""
    )

  }

}
