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
  ) ++ (
    // disable parallel execution in Windows CI runners as a workaround for flaky errors like:
    //   java.io.FileNotFoundException:
    //     C:\Users\runneradmin\AppData\Local\Coursier\Cache\v1\.structure.lock
    //     (The process cannot access the file because it is being used by another process)
    if (
      sys.env
        .get("CI")
        .exists(_.nonEmpty) && sys
        .props("os.name")
        .toLowerCase
        .contains("win")
    ) {
      List(
        Global / concurrentRestrictions := Seq(Tags.limit(Tags.All, 1))
      )
    } else List.empty
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
      addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0"),
      scriptedDependencies := {
        val () = scriptedDependencies.value
        val () = (`sbt-scalajs-vite` / publishLocal).value
      }
    )
    .dependsOn(`sbt-scalajs-vite`)
