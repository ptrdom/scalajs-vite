ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"

lazy val `scalajs-vite` = (project in file("."))
  .aggregate(`sbt-scalajs-vite`, `sbt-web-scalajs-vite`)

lazy val commonSettings = Seq(
  scriptedLaunchOpts ++= Seq(
    "-Dplugin.version=" + version.value
  )
)

lazy val `sbt-scalajs-vite` =
  project
    .in(file("sbt-scalajs-vite"))
    .enablePlugins(SbtPlugin)
    .settings(commonSettings)
    .settings(
      addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1")
    )

lazy val `sbt-web-scalajs-vite` =
  project
    .in(file("sbt-web-scalajs-vite"))
    .enablePlugins(SbtPlugin)
    .settings(commonSettings)
    .settings(
      addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0"),
      scriptedDependencies := {
        val () = scriptedDependencies.value
        val () = (`sbt-scalajs-vite` / publishLocal).value
      }
    )
    .dependsOn(`sbt-scalajs-vite`)
