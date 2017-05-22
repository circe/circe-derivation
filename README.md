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

On a more positive note, it has no dependencies except for circe-core and should compile much more
quickly in most cases.

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
