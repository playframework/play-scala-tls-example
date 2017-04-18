name := """play-scala-tls-example"""

version := "1.0.0"

lazy val one = (project in file("modules/one")).enablePlugins(PlayScala)

lazy val two = (project in file("modules/two")).enablePlugins(PlayScala)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .aggregate(one, two)
  .dependsOn(one, two)

scalaVersion := "2.11.11"

libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

fork in run := true

// Uncomment if you want to run "./play client" explicitly without SNI.
//javaOptions in run += "-Djsse.enableSNIExtension=false"

javaOptions in run += "-Djavax.net.debug=ssl:handshake"

addCommandAlias("client", "runMain Main")
