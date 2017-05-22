organization in ThisBuild := "io.circe"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xfuture"
)

val catsVersion = "0.9.0"
val circeVersion = "0.8.0"
val previousCirceDerivationVersion = "0.7.1"

val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  scalacOptions in (Test, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  coverageHighlighting := true,
  coverageScalacPluginVersion := "1.3.0",
  (scalastyleSources in Compile) ++= (unmanagedSourceDirectories in Compile).value
)

val allSettings = baseSettings ++ publishSettings

val docMappingsApiDir = settingKey[String]("Subdirectory in site target directory for API docs")

val root = project.in(file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(libraryDependencies += "io.circe" %% "circe-jawn" % circeVersion)
  .aggregate(
    derivation, derivationJS,
    examplesDerivation, examplesDerivationJS,
    examplesGeneric, examplesGenericJS
  )
  .dependsOn(derivation)

lazy val derivationBase = crossProject.crossType(CrossType.Pure).in(file("derivation"))
  .settings(allSettings)
  .settings(
    moduleName := "circe-derivation",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion % "test",
      "io.circe" %%% "circe-testing" % circeVersion % "test"
    ),
    ghpagesNoJekyll := true,
    docMappingsApiDir := "api"
  )
  .dependsOn(examplesBase % "test")
  .jvmConfigure(_.copy(id = "derivation"))
  .jsConfigure(_.copy(id = "derivationJS"))
  .jvmSettings(
    mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation" % previousCirceDerivationVersion),
    addMappingsToSiteDir(mappings in (Compile, packageDoc), docMappingsApiDir)
  )
  .jsSettings(
    coverageExcludedPackages := "io.circe.derivation.*"
  )

lazy val derivation = derivationBase.jvm
lazy val derivationJS = derivationBase.js

lazy val examplesBase = crossProject.crossType(CrossType.Pure).in(file("examples"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.5",
      "org.typelevel" %%% "cats-core" % catsVersion
    )
  )
  .jvmConfigure(_.copy(id = "examples"))
  .jsConfigure(_.copy(id = "examplesJS"))

lazy val examples = examplesBase.jvm
lazy val examplesJS = examplesBase.js

lazy val examplesDerivationBase = crossProject.crossType(CrossType.Pure).in(file("examples/derivation"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .dependsOn(derivationBase, examplesBase)
  .jvmConfigure(_.copy(id = "examplesDerivation"))
  .jsConfigure(_.copy(id = "examplesDerivationJS"))
  .jsSettings(
    coverageExcludedPackages := "io.circe.examples.derivation.*"
  )

lazy val examplesDerivation = examplesDerivationBase.jvm
lazy val examplesDerivationJS = examplesDerivationBase.js

lazy val examplesGenericBase = crossProject.crossType(CrossType.Pure).in(file("examples/generic"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion)
  .dependsOn(examplesBase)
  .jvmConfigure(_.copy(id = "examplesGeneric"))
  .jsConfigure(_.copy(id = "examplesGenericJS"))

lazy val examplesGeneric = examplesGenericBase.jvm
lazy val examplesGenericJS = examplesGenericBase.js

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/circe/circe-derivation")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
  )
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
