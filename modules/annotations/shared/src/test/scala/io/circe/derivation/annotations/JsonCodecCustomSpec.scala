/*
 * Copyright 2022 circe
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

package io.circe.derivation.annotations

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

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

class CustomJsonCodecSpec extends AnyWordSpec with Matchers {

  import CustomJsonCodecSpecSamples._

  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  "CustomJsonCodec" should {

    io.circe.Decoder
    "correct generate json" in {
      val p1 = Person("andrea")

      p1.asJson.printWith(printer) should be("{\"n\":\"andrea\"}")
      parse("""{"n":"andrea"}""").flatMap(_.as[Person]) should be(Right(p1))
    }

    "remove empty values" in {
      val p1 = RemoveEmptyPerson()

      p1.asJson.printWith(printer) should be(
        """{"values":[],"valuesSet":[],"obj":{}}"""
      )

      val p2 = RemoveEmptyPerson(values = List("a"))

      p2.asJson.printWith(printer) should be(
        """{"values":["a"],"valuesSet":[],"obj":{}}"""
      )
      parse("""{}""").flatMap(_.as[RemoveEmptyPerson]) should be(Right(p1))
    }

    "manage map" in {
      val g1 = Group("name", Map("peter" -> Person("Peter", 18)))

      g1.asJson.printWith(printer) should be(
        """{"name":"name","team":{"peter":{"n":"Peter","a":18}}}"""
      )

    }

  }
}
