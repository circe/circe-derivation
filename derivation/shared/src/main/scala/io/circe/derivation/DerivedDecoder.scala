package io.circe.derivation

import cats.data.Validated
import io.circe.{ AccumulatingDecoder, Decoder, DecodingFailure }
import scala.collection.mutable.Builder

abstract class DerivedDecoder[A] extends Decoder[A] {
  protected[this] def errors(results: List[AccumulatingDecoder.Result[_]]): List[DecodingFailure] = {
    val invalids: Builder[DecodingFailure, List[DecodingFailure]] = List.newBuilder[DecodingFailure]
    val iterator: Iterator[AccumulatingDecoder.Result[_]] = results.toIterator

    while (iterator.hasNext) {
      iterator.next() match {
        case Validated.Invalid(errors) => invalids ++= errors.toList
        case _ =>
      }
    }

    invalids.result
  }
}
