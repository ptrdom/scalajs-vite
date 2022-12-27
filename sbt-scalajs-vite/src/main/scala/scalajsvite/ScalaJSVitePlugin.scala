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
import sbt.internal.util.ManagedLogger

import scala.jdk.CollectionConverters._
import scala.sys.process.ProcessLogger

object ScalaJSVitePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  object autoImport {
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

  private class ProcessWrapper(
      val process: Process,
      val stdoutThread: Thread,
      val stderrThread: Thread
  )

  private def viteTask(
      stageTask: TaskKey[sbt.Attributed[Report]],
      start: TaskKey[Unit],
      stop: TaskKey[Unit],
      compile: TaskKey[Unit],
      command: String
  ) = {
    var processWrapper: Option[ProcessWrapper] = None

    def terminateProcess(log: Logger) = {
      processWrapper.foreach { processWrapper =>
        log.info(s"Terminating Vite [$command] process")

        processWrapper.stdoutThread.interrupt()
        processWrapper.stderrThread.interrupt()
        // TODO consider using reflection to keep JDK 8 compatibility
        processWrapper.process
          .descendants() // requires JDK 9+
          .forEach(process => process.destroy())
        processWrapper.process.destroy()
      }
      processWrapper = None
    }

    Seq(
      stageTask / start := {
        val logger = state.value.globalLogging.full
        logger.info("Starting Vite")

        (stageTask / stop).value

        (stageTask / compile).value

        val targetDir = (viteInstall / crossTarget).value

        terminateProcess(logger)

        val (p, stdoutThread, stderrThread) =
          vitePackageManager.value.runJava(logger)(command, targetDir)
        processWrapper = Some(new ProcessWrapper(p, stdoutThread, stderrThread))
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
    vitePackageManager := PackageManager.Npm
  ) ++
    inConfig(Compile)(perConfigSettings) ++
    inConfig(Test)(perConfigSettings)

  private lazy val perConfigSettings: Seq[Setting[_]] = Seq(
    viteResourcesDirectory := baseDirectory.value / "vite",
    unmanagedSourceDirectories += viteResourcesDirectory.value,
    viteInstall / crossTarget := {
      crossTarget.value /
        "vite" /
        (if (configuration.value == Compile) "main" else "test")
    },
    viteCopyResources := {
      val s = streams.value

      val targetDir = (viteInstall / crossTarget).value

      def copyChanges(directory: File): Unit = {
        s.log.debug(s"walking ${directory.getAbsolutePath}")
        Files
          .walk(directory.toPath)
          .iterator()
          .asScala
          .map(_.toFile)
          .filter(file => file.getAbsolutePath != directory.getAbsolutePath)
          .foreach { file =>
            if (file.isDirectory) {
              copyChanges(file)
            } else {
              val targetFile = new File(
                file.getAbsolutePath.replace(
                  viteResourcesDirectory.value.getAbsolutePath,
                  targetDir.getAbsolutePath
                )
              )
              if (!Hash(file).sameElements(Hash(targetFile))) {
                s.log.debug(
                  s"File changed [${file.getAbsolutePath}], copying [${targetFile.getAbsolutePath}]"
                )
                IO.copyFile(
                  file,
                  targetFile
                )
                true
              } else {
                s.log.debug(s"File not changed [${file.getAbsolutePath}]")
                false
              }
            }
          }
      }

      copyChanges(viteResourcesDirectory.value)
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
      ) { filesToCopy =>
        filesToCopy
          .filter(_.exists())
          .foreach(file => IO.copyFile(file, targetDir / file.getName))

        vitePackageManager.value.install(s.log)(targetDir)

        IO.copyFile(
          targetDir / lockFile,
          viteResourcesDirectory.value / lockFile
        )

        Set.empty
      }(
        Set(viteResourcesDirectory.value / lockFile)
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

        (stageTask / scalaJSLinkerOutputDirectory).value
          .listFiles()
          .foreach(file => IO.copyFile(file, targetDir / file.name))
      },
      stageTask / viteBuild := {
        (stageTask / viteCompile).value

        val logger = state.value.globalLogging.full

        val targetDir = (viteInstall / crossTarget).value

        vitePackageManager.value
          .build(logger)(targetDir)
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
