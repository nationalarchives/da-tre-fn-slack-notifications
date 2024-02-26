import Dependencies._

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "da-tre-fn-slack-notifications",
    libraryDependencies ++= Seq(
      lambdaRuntimeInterfaceClient
    ),
    assembly / assemblyOutputPath := file("target/function.jar")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % Test,
  "uk.gov.nationalarchives" % "da-transform-schemas" % "2.7",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.4",
  "org.playframework" %% "play-json" % "3.0.2",
  "io.circe" %% "circe-generic-extras" % "0.14.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.14"
)

val circeVersion = "0.14.6"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
