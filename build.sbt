lazy val root = (project in file("."))
  .settings(
    name := "stellarctl",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.6.0",
      "com.typesafe" % "config" % "1.3.1",
      "net.liftweb" %% "lift-json" % "3.1.0",
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "org.scalaj" %% "scalaj-http" % "2.3.0"
    )
  ).enablePlugins(JavaAppPackaging)
