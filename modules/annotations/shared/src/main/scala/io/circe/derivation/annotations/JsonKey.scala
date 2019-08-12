package io.circe.derivation.annotations

import scala.annotation.StaticAnnotation

final case class JsonKey(value: String) extends StaticAnnotation

final case class JsonNoDefault() extends StaticAnnotation