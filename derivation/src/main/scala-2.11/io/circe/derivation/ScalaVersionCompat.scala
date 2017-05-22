package io.circe.derivation

import scala.reflect.macros.blackbox

private[derivation] trait ScalaVersionCompat {
  protected def rightValueName(c: blackbox.Context): c.universe.TermName = c.universe.TermName("b")
  /*protected def extractFromRight(c: blackbox.Context)(value: c.TermName, tpe: c.Type): c.Tree = {
    import c.universe._

    q"$value.asInstanceOf[_root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe]].b"
  }*/
}
