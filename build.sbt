import _root_.io.github.davidgregory084.DevMode

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "functionary",
    libraryDependencies ++= List(
//      "com.disneystreaming" %% "weaver-cats" % "0.8.2" % Test,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
//      "com.eed3si9n.expecty" %% "expecty" % "0.16.0" % Test,
      "com.lihaoyi" %% "sourcecode" % "0.3.0"
    ),
    Compile / sourceGenerators += (Compile / sourceManaged)
      .map(Boilerplate.gen)
      .taskValue
  )

//ThisBuild / testFrameworks += new TestFramework("weaver.framework.CatsEffect")
ThisBuild / testFrameworks += new TestFramework("munit.Framework")
ThisBuild / organization := "com.github.custommonkey"
ThisBuild / crossScalaVersions := List("3.2.2", "2.13.10", "2.12.17")
ThisBuild / versionScheme := Some("early-semver")

lazy val docs = project // new documentation project
  .in(file("myproject-docs")) // important: it must not be docs/
  .settings(
    crossScalaVersions := Nil,
    scalaVersion := "2.13.10",
    tpolecatOptionsMode := _root_.io.github.davidgregory084.DevMode
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin)
