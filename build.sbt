inThisBuild(
  List(
    scalaVersion := "2.12.21",
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
    versionScheme := Some("semver-spec")
  )
)

lazy val `scalajs-vite` = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(`sbt-scalajs-vite`, `sbt-web-scalajs-vite`, `scala-steward-hooks`)

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
      addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.21.0")
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

// workaround for https://github.com/sbt/sbt/issues/7431
TaskKey[Unit]("scriptedSequentialPerModule") := {
  Def.taskDyn {
    val projects: Seq[ProjectReference] = `scalajs-vite`.aggregate
    Def
      .sequential(
        projects.map(p =>
          Def.taskDyn {
            (p / scripted).?.value match {
              case Some(_) => (p / scripted).toTask("")
              case None    => Def.task(())
            }
          }
        )
      )
  }.value
}

// Declares the library dependencies used in the scripted tests so that Scala
// Steward keeps them up to date. The scripted test build definitions are not
// part of this build, so without this project Scala Steward would not discover
// these dependencies. The versions here must match the ones used in the
// scripted tests for Scala Steward's version replacement to reach them.
lazy val `scala-steward-hooks` = project
  .in(file("scala-steward-hooks"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    publish / skip := true,
    scalaVersion := "2.13.17",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.1",
      "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1",
      "org.scalatest" %%% "scalatest" % "3.2.20",
      "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.20",
      "org.scalatestplus" %% "selenium-4-9" % "3.2.16.0",
      "org.seleniumhq.selenium" % "selenium-java" % "4.12.1",
      "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.1",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http" % "10.2.10"
    )
  )
