import _root_.io.github.davidgregory084.DevMode

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "functionary",
    libraryDependencies ++= List(
      "com.disneystreaming" %% "weaver-cats" % "0.8.2" % Test,
      "com.lihaoyi" %% "sourcecode" % "0.3.0"
    ),
    Compile / sourceGenerators += (Compile / sourceManaged)
      .map(Boilerplate.gen)
      .taskValue
  )

ThisBuild / testFrameworks += new TestFramework("weaver.framework.CatsEffect")
ThisBuild / organization := "com.github.custommonkey"
ThisBuild / crossScalaVersions := List("3.2.2", "2.13.10")
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

lazy val cats = project
  .settings(
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.8"
  )
  .dependsOn(root)
