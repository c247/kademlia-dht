ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "kademliadht"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.16"
)

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.14"
