inThisBuild(
  List(
    scalaVersion := "2.12.17",
    organization := "me.ptrdom",
    homepage := Some(url("https://github.com/ptrdom/scalajs-vite")),
    licenses := List(License.MIT),
    developers := List(
      Developer(
        "ptrdom",
        "Domantas Petrauskas",
        "dom.petrauskas@gmail.com",
        url("http://ptrdom.me/")
      )
    ),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    versionScheme := Some("semver-spec")
  )
)

lazy val `scalajs-vite` = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(`sbt-scalajs-vite`, `sbt-web-scalajs-vite`)

lazy val commonSettings = Seq(
  scriptedLaunchOpts ++= Seq(
    "-Dplugin.version=" + version.value
  ),
  scriptedBufferLog := false
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
      addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0-0.6"),
      scriptedDependencies := {
        val () = scriptedDependencies.value
        val () = (`sbt-scalajs-vite` / publishLocal).value
      }
    )
    .dependsOn(`sbt-scalajs-vite`)
