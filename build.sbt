ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "functionary",
    libraryDependencies += "com.disneystreaming" %% "weaver-cats" % "0.8.2" % Test
  )

ThisBuild / testFrameworks += new TestFramework("weaver.framework.CatsEffect")
ThisBuild / organization := "com.github.custommonkey"
ThisBuild / crossScalaVersions := List("3.2.2", "2.13.10")
