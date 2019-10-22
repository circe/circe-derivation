import sbtcrossproject.{ CrossType, crossProject }
import scala.xml.{ Elem, Node => XmlNode, NodeSeq => XmlNodeSeq }
import scala.xml.transform.{ RewriteRule, RuleTransformer }

organization in ThisBuild := "io.circe"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)

val catsVersion = "2.0.0"
val circeVersion = "0.12.3"
val paradiseVersion = "2.1.1"
val previousCirceDerivationVersion = "0.12.0-M5"
val scalaCheckVersion = "1.14.2"
val scalaJavaTimeVersion = "2.0.0-RC3"

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions ++= (
    if (priorTo2_13(scalaVersion.value))
      Seq(
        "-Xfuture",
        "-Yno-adapted-args",
        "-Ywarn-unused-import"
      )
    else
      Seq(
        "-Ywarn-unused:imports"
      )
  ),
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  scalacOptions in (Test, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  coverageHighlighting := true,
  (scalastyleSources in Compile) ++= (unmanagedSourceDirectories in Compile).value
)

val allSettings = baseSettings ++ publishSettings

val docMappingsApiDir = settingKey[String]("Subdirectory in site target directory for API docs")

val root = project
  .in(file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-jawn" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion
    )
  )
  .aggregate(
    derivationJVM,
    derivationJS,
    annotationsJVM,
    annotationsJS,
    examplesScrooge,
    examplesDerivationJVM,
    examplesDerivationJS,
    examplesGenericJVM,
    examplesGenericJS
  )
  .dependsOn(derivationJVM)

lazy val derivation = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/derivation"))
  .settings(allSettings)
  .settings(
    name := "Circe derivation",
    moduleName := "circe-derivation",
    description := "circe derivation",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "io.circe" %%% "circe-parser" % circeVersion % Test,
      "io.circe" %%% "circe-testing" % circeVersion % Test,
      "org.scalatestplus" %%% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test
    ),
    ghpagesNoJekyll := true,
    docMappingsApiDir := "api"
  )
  .dependsOn(examples % Test)
  .jvmSettings(
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          "com.stripe" %% "scrooge-shapes" % "0.1.0" % Test
        )
      else Nil
    ),
    mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation" % previousCirceDerivationVersion),
    addMappingsToSiteDir(mappings in (Compile, packageDoc), docMappingsApiDir)
  )
  .jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := "io.circe.derivation.*",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
  )
  .jvmConfigure(_.dependsOn(examplesScrooge % Test))

lazy val derivationJVM = derivation.jvm
lazy val derivationJS = derivation.js

lazy val annotations = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/annotations"))
  .settings(allSettings)
  .settings(
    name := "Circe derivation",
    moduleName := "circe-derivation-annotations",
    description := "circe derivation annotations",
    ghpagesNoJekyll := true,
    docMappingsApiDir := "api",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided
    ),
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          compilerPlugin(("org.scalamacros" % "paradise" % paradiseVersion).cross(CrossVersion.patch))
        )
      else Nil
    ),
    scalacOptions ++= (
      if (priorTo2_13(scalaVersion.value)) Nil else Seq("-Ymacro-annotations")
    )
  )
  .dependsOn(derivation, derivation % "test->test")
  .jvmSettings(
    mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation-annotations" % previousCirceDerivationVersion),
    addMappingsToSiteDir(mappings in (Compile, packageDoc), docMappingsApiDir)
  )
  .jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := "io.circe.derivation.*",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
  )
  .jvmConfigure(_.dependsOn(examplesScrooge % Test))

lazy val annotationsJVM = annotations.jvm
lazy val annotationsJS = annotations.js

lazy val examples = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %%% "cats-core" % catsVersion
    )
  )
  .jsSettings(
    coverageEnabled := false
  )

lazy val examplesJVM = examples.jvm
lazy val examplesJS = examples.js

lazy val examplesScrooge = project
  .in(file("examples/scrooge"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %% "cats-core" % catsVersion
    ),
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          "com.twitter" %% "scrooge-core" % "19.8.0",
          "org.apache.thrift" % "libthrift" % "0.10.0"
        )
      else Nil
    ),
    scroogeThriftSourceFolder in Compile := (
      if (priorTo2_13(scalaVersion.value))
        (
          baseDirectory.value / "src" / "main" / "thrift"
        )
      else
        (
          // A hack to avoid generating Scrooge source on 2.13 for now.
          baseDirectory.value / "_none_"
        )
    )
  )

lazy val examplesDerivation = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/derivation"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    coverageExcludedPackages := "io.circe.examples.*",
    wartremoverErrors ++= Warts.unsafe
  )
  .jsSettings(
    coverageEnabled := false
  )
  .jvmConfigure(_.dependsOn(examplesScrooge))
  .dependsOn(derivation, examples)

lazy val examplesDerivationJVM = examplesDerivation.jvm
lazy val examplesDerivationJS = examplesDerivation.js

lazy val examplesGeneric = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/generic"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    coverageExcludedPackages := "io.circe.examples.*"
  )
  .settings(libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion)
  .jvmSettings(
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          "com.stripe" %% "scrooge-shapes" % "0.1.0"
        )
      else Nil
    )
  )
  .jsSettings(
    coverageEnabled := false
  )
  .jvmConfigure(_.dependsOn(examplesScrooge))
  .dependsOn(examples)

lazy val examplesGenericJVM = examplesGeneric.jvm
lazy val examplesGenericJS = examplesGeneric.js

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/circe/circe-derivation")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  autoAPIMappings := true,
  apiURL := Some(url("https://circe.github.io/circe-derivation/api/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/circe/circe-derivation"),
      "scm:git:git@github.com:circe/circe-derivation.git"
    )
  ),
  developers := List(
    Developer(
      "travisbrown",
      "Travis Brown",
      "travisrobertbrown@gmail.com",
      url("https://twitter.com/travisbrown")
    )
  ),
  pomPostProcess := { (node: XmlNode) =>
    new RuleTransformer(
      new RewriteRule {
        private def isTestScope(elem: Elem): Boolean =
          elem.label == "dependency" && elem.child.exists(child => child.label == "scope" && child.text == "test")

        override def transform(node: XmlNode): XmlNodeSeq = node match {
          case elem: Elem if isTestScope(elem) => Nil
          case _                               => node
        }
      }
    ).transform(node).head
  }
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
).toSeq
