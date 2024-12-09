ThisBuild / scalaVersion     := "2.13.15"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.mehmetcc"
ThisBuild / organizationName := "example"

val ZioVersion       = "2.1.4"
val ZioConfigVersion = "4.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "file-streaming",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"                 % ZioVersion,
      "dev.zio" %% "zio-streams"         % ZioVersion,
      "dev.zio" %% "zio-config"          % ZioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % ZioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % ZioConfigVersion,
      "dev.zio" %% "zio-test"            % ZioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
