name := "slick-postgres-helpers"

organization := "com.snapswap"

version := "1.0.5"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-encoding",
  "UTF-8"
)

libraryDependencies ++= {
  val slickV = "3.2.1"
  val slickPgV = "0.15.3"
  Seq(
    "org.postgresql" % "postgresql" % "42.1.4" % "provided",
    "com.typesafe.slick" %% "slick" % slickV % "provided",
    "com.github.tminglei" %% "slick-pg" % slickPgV,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPgV,
    "com.opentable.components" % "otj-pg-embedded" % "0.9.0" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test"
  )
}

fork in Test := true
