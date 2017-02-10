name := "slick-postgres-helpers"

organization := "com.snapswap"

version := "1.0.1"

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
  val akkaV = "2.4.11"
  Seq(
    "org.postgresql" % "postgresql" % "9.4.1212" % "provided",
    "com.typesafe.slick" %% "slick" % "3.2.0-M1" % "provided",
    "com.opentable.components" % "otj-pg-embedded" % "0.7.1" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test"
  )
}

fork in Test := true
