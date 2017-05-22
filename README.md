# circe-derivation

[![Build status](https://img.shields.io/travis/circe/circe-derivation/master.svg)](https://travis-ci.org/circe/circe-derivation)
[![Coverage status](https://img.shields.io/codecov/c/github/circe/circe-derivation/master.svg)](https://codecov.io/github/circe/circe-derivation)
[![Gitter](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/circe/circe)
[![Maven Central](https://img.shields.io/maven-central/v/io.circe/circe-derivation_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/io.circe/circe-derivation_2.12)

This library provides macro-supported derivation of circe's type class instances. It differs from
circe-generic in that this derivation is not based on Shapeless's generic representation of case
classes and ADTs, and it is not intended to be as fully featured as `io.circe.generic`. In
particular:

* It's not published for Scala 2.10.
* It only supports "semi-automatic" derivation.

On a more positive note, it has no dependencies apart from circe-core and should compile much more
quickly in most cases.

## Who is this for?

There are already a lot of ways to define encoders and decoders in circe (or to avoid defining
them using fully-automatic derivation). In general users who want to use generic derivation should
stick to circe-generic, which is well-tested, robust, and provides a clear path from automatic
generic derivation (which can be useful for initial exploration or in relatively simple
applications) to semi-automatic derivation (which has some syntactic overhead but tends to compile
much more quickly and to be easier to reason about).

Users who want the generic derivation experience but need particular functionality that isn't
provided in circe-generic may be interested in circe-generic-extras, which supports transforming
member names, using default values, etc. (at the expense of even slower compile times).

This library is for people who don't care about the full generic derivation experience but just
want fast builds and instances that stay in sync with their definitions, and who don't mind a bit
of boilerplate (a couple of lines per case class).

## Contributors and participation

This project supports the Scala [code of conduct][code-of-conduct] and we want
all of its channels (Gitter, GitHub, etc.) to be welcoming environments for everyone.

Please see the [circe contributors' guide][contributing] for details on how to submit a pull
request.

## License

circe-derivation is licensed under the **[Apache License, Version 2.0][apache]**
(the "License"); you may not use this software except in compliance with the
License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[apache]: http://www.apache.org/licenses/LICENSE-2.0
[api-docs]: https://circe.github.io/circe-derivation/api/io/circe/
[circe]: https://github.com/circe/circe
[code-of-conduct]: https://www.scala-lang.org/conduct.html
[contributing]: https://circe.github.io/circe/contributing.html
