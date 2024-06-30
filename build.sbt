ThisBuild / version := "0.1.0-SNAPSHOT"

val SCALA_213 = "2.13.11"
val SCALA_212 = "2.12.19"
val SCALA_33 = "3.3.3"

ThisBuild / scalaVersion := SCALA_213

ThisBuild / githubWorkflowJavaVersions := JavaSpec.temurin("17") :: Nil

lazy val root = (project in file("."))
  .settings(
    name := "functionary",
    libraryDependencies ++= List(
//      "com.disneystreaming" %% "weaver-cats" % "0.8.2" % Test,
      "org.scalameta" %% "munit" % "1.0.0" % Test,
//      "com.eed3si9n.expecty" %% "expecty" % "0.16.0" % Test,
      "com.lihaoyi" %% "sourcecode" % "0.4.2"
    ),
    Compile / sourceGenerators += (Compile / sourceManaged)
      .map(Boilerplate.gen)
      .taskValue
  )

ThisBuild / testFrameworks += new TestFramework("munit.Framework")
ThisBuild / organization := "com.github.custommonkey"
ThisBuild / crossScalaVersions := List(SCALA_33, SCALA_213, SCALA_212)
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / githubOwner := "custommonkey"
ThisBuild / githubRepository := "functionary"

lazy val docs = project // new documentation project
  .in(file("myproject-docs")) // important: it must not be docs/
  .settings(
    githubWorkflowArtifactUpload := false,
    crossScalaVersions := Nil,
    scalaVersion := SCALA_213
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin)
  .disablePlugins(TpolecatPlugin)
