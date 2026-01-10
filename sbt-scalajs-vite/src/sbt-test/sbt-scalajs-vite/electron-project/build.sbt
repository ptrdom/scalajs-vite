import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.linker.interface.ModuleInitializer
import org.scalajs.sbtplugin.Stage
import scala.sys.process._

lazy val root = (project in file("."))
  .aggregate(app, `integration-test`)

ThisBuild / scalaVersion := "2.13.17"

val viteElectronBuildPackage =
  taskKey[Unit]("Generate package directory with electron-builder")
val viteElectronBuildDistributable =
  taskKey[Unit]("Package in distributable format with electron-builder")

lazy val app = (project in file("app"))
  .enablePlugins(ScalaJSVitePlugin)
  .settings(
    Test / test := {},
    // Suppress meaningless 'multiple main classes detected' warning
    Compile / mainClass := None,
    scalaJSModuleInitializers := Seq(
      ModuleInitializer
        .mainMethodWithArgs("example.Main", "main")
        .withModuleID("main"),
      ModuleInitializer
        .mainMethodWithArgs("example.Preload", "main")
        .withModuleID("preload"),
      ModuleInitializer
        .mainMethodWithArgs("example.Renderer", "main")
        .withModuleID("renderer")
    ),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",
    Seq(Compile, Test)
      .flatMap { config =>
        inConfig(config)(
          Seq(Stage.FastOpt, Stage.FullOpt).flatMap { stage =>
            val stageTask = stage match {
              case Stage.FastOpt => fastLinkJS
              case Stage.FullOpt => fullLinkJS
            }

            def fn(args: List[String] = Nil) = Def.task {
              val log = streams.value.log

              (stageTask / viteBuild).value

              val targetDir = (viteInstall / crossTarget).value

              val exitValue = Process(
                "node" :: "node_modules/electron-builder/cli" :: Nil ::: args,
                targetDir
              ).run(log).exitValue()
              if (exitValue != 0) {
                sys.error(s"Nonzero exit value: $exitValue")
              } else ()
            }

            Seq(
              stageTask / viteElectronBuildPackage := fn("--dir" :: Nil).value,
              stageTask / viteElectronBuildDistributable := fn().value
            )
          }
        )
      }
  )

lazy val `integration-test` = (project in file("integration-test"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
    },
    Test / jsEnv := Def.taskDyn {
      val stageTask = (app / Compile / scalaJSStage).value match {
        case Stage.FastOpt => fastLinkJS
        case Stage.FullOpt => fullLinkJS
      }

      Def.task {
        (((app / Compile) / stageTask) / viteBuild).value

        val sourcesDirectory =
          (((app / Compile) / viteInstall) / crossTarget).value
        val mainModuleID = "main"

        val nodePath = (sourcesDirectory / "node_modules").absolutePath
        val mainPath =
          (sourcesDirectory / "dist-electron" / s"$mainModuleID.js").absolutePath

        new NodeJSEnv(
          NodeJSEnv
            .Config()
            .withEnv(
              Map(
                "NODE_PATH" -> nodePath,
                "MAIN_PATH" -> mainPath
              )
            )
        )
      }
    }.value,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0" % "test",
      "org.scalatest" %%% "scalatest" % "3.2.15" % "test"
    )
  )
