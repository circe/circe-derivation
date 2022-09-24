import sbtcrossproject.{ CrossType, crossProject }

val Scala212V: String = "2.12.15"
val Scala213V: String = "2.13.8"
// val Scala3V: String = "3.1.3" - unsupported yet

ThisBuild / circeRootOfCodeCoverage := Some("rootJVM")
ThisBuild / tlBaseVersion := "0.13"
ThisBuild / tlCiReleaseTags := true
ThisBuild / tlFatalWarningsInCi := false // we currently have a lot of warnings that will need to be fixed

ThisBuild / organization := "io.circe"
ThisBuild / crossScalaVersions := List(Scala212V, Scala213V) // List(Scala3V, Scala212V, Scala213V)
ThisBuild / scalaVersion := Scala213V

ThisBuild / githubWorkflowJavaVersions := Seq("8", "11", "17").map(JavaSpec.temurin)

val catsVersion = "2.7.0"
val circeVersion = "0.14.1"
val paradiseVersion = "2.1.1"
val previousCirceDerivationVersion = "0.13.0-M5"
val scalaCheckVersion = "1.15.4"
val scalaJavaTimeVersion = "2.3.0"

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

val baseSettings = Seq(
  coverageHighlighting := true
)

val allSettings = baseSettings

val root = tlCrossRootProject
  .enablePlugins(NoPublishPlugin)
  .settings(allSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-jawn" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion
    )
  )
  .aggregate(
    derivation.jvm,
    derivation.js,
    annotations.jvm,
    annotations.js,
    examplesScrooge,
    examplesDerivation.jvm,
    examplesDerivation.js,
    examplesGeneric.jvm,
    examplesGeneric.js
  )

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
      "org.scalatestplus" %%% "scalacheck-1-14" % "3.2.2.0" % Test,
      "org.typelevel" %%% "discipline-scalatest" % "2.1.5" % Test
    )
  )
  .dependsOn(examples % Test)
  .jvmSettings(
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          "com.stripe" %% "scrooge-shapes" % "0.1.0" % Test
        )
      else Nil
    )
    // mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation" % previousCirceDerivationVersion)
  )
  .jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := "io.circe.derivation.*",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
  )
  .jvmConfigure(_.dependsOn(examplesScrooge % Test))

lazy val annotations = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/annotations"))
  .settings(allSettings)
  .settings(
    name := "Circe derivation",
    moduleName := "circe-derivation-annotations",
    description := "circe derivation annotations",
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
      if (priorTo2_13(scalaVersion.value)) Nil
      else Seq("-Ymacro-annotations")
    )
  )
  .dependsOn(derivation, derivation % "test->test")
  .jvmSettings(
    mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation-annotations" % previousCirceDerivationVersion)
  )
  .jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := "io.circe.derivation.*",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion
  )
  .jvmConfigure(_.dependsOn(examplesScrooge % Test))

lazy val examples = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples"))
  .settings(allSettings)
  .enablePlugins(NoPublishPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-testing" % circeVersion,
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %%% "cats-core" % catsVersion
    )
  )
  .jsSettings(
    coverageEnabled := false
  )

lazy val examplesScrooge = project
  .in(file("examples/scrooge"))
  .settings(allSettings)
  .enablePlugins(NoPublishPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %% "cats-core" % catsVersion
    ),
    libraryDependencies ++= (
      if (priorTo2_13(scalaVersion.value))
        Seq(
          "com.twitter" %% "scrooge-core" % "20.5.0",
          "org.apache.thrift" % "libthrift" % "0.15.0"
        )
      else Nil
    ),
    Compile / scroogeThriftSourceFolder := (
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
  .enablePlugins(NoPublishPlugin)
  .settings(
    coverageExcludedPackages := "io.circe.examples.*"
  )
  .jsSettings(
    coverageEnabled := false
  )
  .jvmConfigure(_.dependsOn(examplesScrooge))
  .dependsOn(derivation, examples)

lazy val examplesGeneric = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/generic"))
  .settings(allSettings)
  .enablePlugins(NoPublishPlugin)
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
