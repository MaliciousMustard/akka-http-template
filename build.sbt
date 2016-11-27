import com.typesafe.sbt.packager.docker.Cmd

name := """project-name"""
organization := "com.beyond"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

val akkaV = "2.4.11"
val scalaTestV = "3.0.0"
val slickV = "3.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",

  "ch.qos.logback" % "logback-classic" % "1.1.7",

  "com.google.inject" % "guice" % "4.0",
  "net.codingwell" %% "scala-guice" % "4.1.0",

  "org.scalatest" %% "scalatest" % scalaTestV % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "joda-time" % "joda-time" % "2.9",
  "org.joda" % "joda-convert" % "1.8.1"
)

lazy val FunctionalTest = config("functional") extend Test

lazy val gitHeadCommitSha = settingKey[String]("current git commit SHA")
lazy val dockerSettings = Seq(
  dockerExposedPorts in Docker := Seq(8087),
  dockerRepository in Docker := Some("016608567734.dkr.ecr.eu-west-1.amazonaws.com"),
  dockerBaseImage in Docker := "anapsix/alpine-java",
  bashScriptExtraDefines ++= Seq(
    """addJava "-Xms$JAVA_MIN_MEM"""",
    """addJava "-Xmx$JAVA_MAX_MEM"""",
    """addJava "-XX:+UseConcMarkSweepGC"""",
    """addJava "-DENV=$ENVIRONMENT"""",
    """for i in $EXTRA_JAVA_PARAMS; do addJava "$i"; done"""
  ),
  gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head,
  version in Docker := "1.0-" + gitHeadCommitSha.value.take(16),
  mappings in Universal += file("manifest_properties.txt") -> "manifest_properties.txt",
  dockerCommands in Docker := dockerCommands.value.flatMap {
    case cmd@Cmd("FROM", _) =>
      List(Cmd("FROM", "anapsix/alpine-java")) ++ List(
        Cmd("ENV", "JAVA_MIN_MEM", "1024m"),
        Cmd("ENV", "JAVA_MAX_MEM", "2048m"),
        Cmd("ENV", "ENVIRONMENT", "prod"),
        Cmd("ENV", "AWS_ACCESS_KEY_ID", "x"),
        Cmd("ENV", "AWS_SECRET_KEY", "x"),
        Cmd("ENV", "EXTRA_JAVA_PARAMS", "-Xss1024k") /*cheat to add a default parameter here, it is harmless to override EXTRA_JAVA_PARAMS with anything*/
      )
    case rest => List(rest)
  }
)

lazy val root = (project in file(".")).
  configs(FunctionalTest).
  settings(inConfig(FunctionalTest)(Defaults.testSettings): _*).
  settings(parallelExecution in FunctionalTest := false).
  settings(dockerSettings).
  settings(
    mainClass in (Compile) := Some("com.beyond.run.WebServer")
  ).
  enablePlugins(JavaAppPackaging, DockerPlugin, UniversalPlugin)

Revolver.settings
