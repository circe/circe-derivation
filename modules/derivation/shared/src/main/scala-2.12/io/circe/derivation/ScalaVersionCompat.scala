/*
 * Copyright 2017 circe
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

package io.circe.derivation

import scala.reflect.macros.blackbox

private[derivation] trait ScalaVersionCompat {
  protected def rightValueName(c: blackbox.Context): c.universe.TermName = c.universe.TermName("value")

  /*protected def extractFromRight(c: blackbox.Context)(value: c.TermName, tpe: c.Type): c.Tree = {
    import c.universe._

    q"$value.asInstanceOf[_root_.scala.Right[_root_.io.circe.DecodingFailure, $tpe]].value"
  }*/
}
