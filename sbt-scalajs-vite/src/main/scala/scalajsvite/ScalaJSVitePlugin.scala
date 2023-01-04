package scalajsvite

import java.nio.file.Files

import org.scalajs.linker.interface.Report
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.ModuleKind
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fastLinkJS
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fullLinkJS
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSLinkerConfig
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSLinkerOutputDirectory
import sbt._
import sbt.AutoPlugin
import sbt.Keys._

import scala.jdk.CollectionConverters._
import scala.sys.process.Process

object ScalaJSVitePlugin extends AutoPlugin {

  override def requires: Plugins = ScalaJSPlugin

  object autoImport {
    val viteRunner: SettingKey[ViteRunner] =
      settingKey[ViteRunner]("Runs Vite commands")
    val vitePackageManager: SettingKey[PackageManager] =
      settingKey[PackageManager]("Package manager to use for Vite tasks")
    val viteResourcesDirectory: SettingKey[sbt.File] =
      settingKey[File]("Vite resource directory")
    val viteCopyResources: TaskKey[Unit] =
      taskKey[Unit]("Copies over Vite resources to target directory")
    val viteInstall: TaskKey[Unit] =
      taskKey[Unit](
        "Runs package manager's `install` in target directory on copied over Vite resources"
      )
    val viteCompile: TaskKey[Unit] =
      taskKey[Unit](
        "Compiles module and copies output to target directory"
      )

    val startVite: TaskKey[Unit] =
      taskKey[Unit]("Runs `vite` on target directory")
    val stopVite: TaskKey[Unit] =
      taskKey[Unit]("Stops running `vite` on target directory")

    val viteBuild: TaskKey[Unit] =
      taskKey[Unit]("Runs `vite build` on target directory")

    val startVitePreview: TaskKey[Unit] =
      taskKey[Unit]("Runs `vite preview` on target directory")
    val stopVitePreview: TaskKey[Unit] =
      taskKey[Unit]("Stops running `vite preview` on target directory")
  }

  import autoImport._

  sealed trait Stage
  object Stage {
    case object FullOpt extends Stage
    case object FastOpt extends Stage
  }

  private def copyChanges(
      logger: Logger
  )(
      sourceDirectory: File,
      targetDirectory: File,
      currentDirectory: File
  ): Unit = {
    logger.debug(s"Walking directory [${currentDirectory.getAbsolutePath}]")
    Files
      .walk(currentDirectory.toPath)
      .iterator()
      .asScala
      .map(_.toFile)
      .filter(file => file.getAbsolutePath != currentDirectory.getAbsolutePath)
      .foreach { file =>
        if (file.isDirectory) {
          copyChanges(logger)(sourceDirectory, targetDirectory, file)
        } else {
          val targetFile = new File(
            file.getAbsolutePath.replace(
              sourceDirectory.getAbsolutePath,
              targetDirectory.getAbsolutePath
            )
          )
          if (!Hash(file).sameElements(Hash(targetFile))) {
            logger.debug(
              s"File changed [${file.getAbsolutePath}], copying to [${targetFile.getAbsolutePath}]"
            )
            IO.copyFile(
              file,
              targetFile
            )
          } else {
            logger.debug(s"File not changed [${file.getAbsolutePath}]")
          }
        }
      }
  }

  private def viteTask(
      stageTask: TaskKey[sbt.Attributed[Report]],
      start: TaskKey[Unit],
      stop: TaskKey[Unit],
      compile: TaskKey[Unit],
      command: String
  ) = {
    var process: Option[Process] = None

    def terminateProcess(log: Logger) = {
      process.foreach { process =>
        log.info(s"Stopping Vite [$command] process")
        process.destroy()
      }
      process = None
    }

    Seq(
      stageTask / start := {
        val logger = state.value.globalLogging.full

        (stageTask / stop).value

        (stageTask / compile).value

        val targetDir = (viteInstall / crossTarget).value

        logger.info(s"Starting Vite [$command] process")
        process = Some(viteRunner.value.process(logger)(command, targetDir))
      },
      stageTask / stop := {
        terminateProcess(streams.value.log)
      },
      (onLoad in Global) := {
        (onLoad in Global).value.compose(
          _.addExitHook {
            terminateProcess(Keys.sLog.value)
          }
        )
      }
    )
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    viteRunner := ViteRunner.Default,
    viteResourcesDirectory := baseDirectory.value / "vite",
    vitePackageManager := PackageManager.Npm
  ) ++
    inConfig(Compile)(perConfigSettings) ++
    inConfig(Test)(perConfigSettings)

  private lazy val perConfigSettings: Seq[Setting[_]] = Seq(
    unmanagedSourceDirectories += viteResourcesDirectory.value,
    viteInstall / crossTarget := {
      crossTarget.value /
        "vite" /
        (if (configuration.value == Compile) "main" else "test")
    },
    viteCopyResources := {
      val s = streams.value

      val targetDir = (viteInstall / crossTarget).value

      copyChanges(s.log)(
        viteResourcesDirectory.value,
        targetDir,
        viteResourcesDirectory.value
      )
    },
    watchSources := (watchSources.value ++ Seq(
      Watched.WatchSource(viteResourcesDirectory.value)
    )),
    viteInstall := {
      viteCopyResources.value

      val s = streams.value

      val targetDir = (viteInstall / crossTarget).value

      val lockFile = vitePackageManager.value.lockFile

      FileFunction.cached(
        streams.value.cacheDirectory /
          "vite" /
          (if (configuration.value == Compile) "main" else "test"),
        inStyle = FilesInfo.hash
      ) { _ =>
        s.log.debug(s"Installing packages")

        vitePackageManager.value.install(s.log)(targetDir)

        IO.copyFile(
          targetDir / lockFile,
          viteResourcesDirectory.value / lockFile
        )

        Set.empty
      }(
        Set(
          viteResourcesDirectory.value / "package.json",
          viteResourcesDirectory.value / lockFile
        )
      )
    }
  ) ++
    perScalaJSStageSettings(Stage.FastOpt) ++
    perScalaJSStageSettings(Stage.FullOpt)

  private def perScalaJSStageSettings(stage: Stage): Seq[Setting[_]] = {
    val stageTask = stage match {
      case Stage.FastOpt => fastLinkJS
      case Stage.FullOpt => fullLinkJS
    }

    Seq(
      stageTask / viteCompile := {
        viteInstall.value

        val targetDir = (viteInstall / crossTarget).value

        stageTask.value

        copyChanges(streams.value.log)(
          (stageTask / scalaJSLinkerOutputDirectory).value,
          targetDir,
          (stageTask / scalaJSLinkerOutputDirectory).value
        )
      },
      stageTask / viteBuild := {
        (stageTask / viteCompile).value

        val logger = state.value.globalLogging.full

        val targetDir = (viteInstall / crossTarget).value

        // TODO cache based on stage task output and Vite resources
        viteRunner.value
          .process(logger)("build", targetDir)
          .exitValue()
      }
    ) ++ viteTask(
      stageTask,
      startVite,
      stopVite,
      viteCompile,
      "dev"
    ) ++ viteTask(
      stageTask,
      startVitePreview,
      stopVitePreview,
      viteBuild,
      "preview"
    )
  }
}
