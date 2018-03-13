import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }

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

val catsVersion = "1.0.1"
val circeVersion = "0.9.1"
val previousCirceDerivationVersion = "0.9.0-M1"

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

def crossModule(path: String, crossType: CrossType = CrossType.Pure) = {
  val id = path.split("/").reduce(_ + _.capitalize)

  CrossProject(jvmId = id, jsId = id + "JS", file(path), crossType)
}

val root = project.in(file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-jawn" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion
    )
  )
  .aggregate(
    derivation, derivationJS,
    examplesScrooge,
    examplesDerivation, examplesDerivationJS,
    examplesGeneric, examplesGenericJS
  )
  .dependsOn(derivation)

lazy val derivationBase = crossModule("derivation", CrossType.Full)
  .settings(allSettings)
  .settings(
    name := "Circe derivation",
    moduleName := "circe-derivation",
    description := "circe derivation",
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
  .jvmSettings(
    mimaPreviousArtifacts := Set("io.circe" %% "circe-derivation" % previousCirceDerivationVersion),
    addMappingsToSiteDir(mappings in (Compile, packageDoc), docMappingsApiDir)
  )
  .jsSettings(
    coverageExcludedPackages := "io.circe.derivation.*"
  )
  .jvmConfigure(_.dependsOn(examplesScrooge % "test"))

lazy val derivation = derivationBase.jvm
lazy val derivationJS = derivationBase.js

lazy val examplesBase = crossModule("examples")
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.5",
      "org.typelevel" %%% "cats-core" % catsVersion
    )
  )

lazy val examples = examplesBase.jvm
lazy val examplesJS = examplesBase.js

lazy val examplesScrooge = project.in(file("examples/scrooge"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "scrooge-core" % "18.3.0" exclude("com.twitter", "libthrift"),
      "org.apache.thrift" % "libthrift" % "0.9.2",
      "org.scalacheck" %% "scalacheck" % "1.13.5",
      "org.typelevel" %% "cats-core" % catsVersion
    )
  )

lazy val examplesDerivationBase = crossModule("examples/derivation", CrossType.Full)
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    coverageExcludedPackages := "io.circe.examples.*"
  )
  .jvmConfigure(_.dependsOn(examplesScrooge))
  .dependsOn(derivationBase, examplesBase)

lazy val examplesDerivation = examplesDerivationBase.jvm
lazy val examplesDerivationJS = examplesDerivationBase.js

lazy val examplesGenericBase = crossModule("examples/generic", CrossType.Full)
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    coverageExcludedPackages := "io.circe.examples.*"
  )
  .settings(libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion)
  .jvmSettings(libraryDependencies += "com.stripe" %% "scrooge-shapes" % "0.1.0")
  .jvmConfigure(_.dependsOn(examplesScrooge))
  .dependsOn(examplesBase)

lazy val examplesGeneric = examplesGenericBase.jvm
lazy val examplesGenericJS = examplesGenericBase.js

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
