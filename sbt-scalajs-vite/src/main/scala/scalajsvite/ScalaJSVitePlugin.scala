package scalajsvite

import java.nio.file.Files

import org.scalajs.linker.interface.Report
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fastLinkJS
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fullLinkJS
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSLinkerOutputDirectory
import sbt._
import sbt.AutoPlugin
import sbt.Keys._
import sbt.internal.util.ManagedLogger

import scala.jdk.CollectionConverters._
import scala.sys.process.{Process => ScalaProcess}
import scala.sys.process.ProcessLogger

object ScalaJSVitePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  object autoImport {
    val viteCopyResources: TaskKey[Unit] = taskKey[Unit]("")
    val viteInstall: TaskKey[Unit] =
      taskKey[Unit](
        "Copies over resources to target directory and runs `npm install`"
      )
    val viteCompile: TaskKey[Unit] =
      taskKey[Unit](
        "Compiles module and copies output to target directory."
      )

    val startVite = taskKey[Unit]("Runs `vite` on target directory.")
    val stopVite = taskKey[Unit]("Stops running `vite` on target directory.")

    val viteBuild = taskKey[Unit]("Runs `vite build` on target directory.")

    val startVitePreview =
      taskKey[Unit]("Runs `vite preview` on target directory.")
    val stopVitePreview =
      taskKey[Unit]("Stops running `vite preview` on target directory.")
  }

  import autoImport._

  sealed trait Stage
  object Stage {
    case object FullOpt extends Stage
    case object FastOpt extends Stage
  }

  private def cmd(name: String) = sys.props("os.name").toLowerCase match {
    case os if os.contains("win") => "cmd" :: "/c" :: name :: Nil
    case _                        => name :: Nil
  }

  private def eagerLogger(log: ManagedLogger) = {
    ProcessLogger(
      out => log.info(out),
      err => log.error(err)
    )
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

        (stageTask / viteCompile).value

        val targetDir = (viteInstall / crossTarget).value

        terminateProcess(logger)

        // using Java Process to use `descendants`
        val pb =
          new ProcessBuilder(cmd("npm") ::: "run" :: command :: Nil: _*)
        pb.directory(targetDir)
        val p = pb.start()
        val stdoutThread = new Thread() {
          override def run(): Unit = {
            scala.io.Source
              .fromInputStream(p.getInputStream)
              .getLines
              .foreach(msg => logger.info(msg))
          }
        }
        stdoutThread.start()
        val stderrThread = new Thread() {
          override def run(): Unit = {
            scala.io.Source
              .fromInputStream(p.getErrorStream)
              .getLines
              .foreach(msg => logger.error(msg))
          }
        }
        stderrThread.start()
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

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings) ++
      inConfig(Test)(perConfigSettings)

  private lazy val perConfigSettings: Seq[Setting[_]] = Seq(
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
                  (baseDirectory.value / "vite").getAbsolutePath,
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
      copyChanges(baseDirectory.value / "vite")
    },
    watchSources := (watchSources.value ++ Seq(
      Watched.WatchSource(baseDirectory.value / "vite")
    )),
    viteInstall := {
      viteCopyResources.value

      val s = streams.value

      val targetDir = (viteInstall / crossTarget).value

      val lockFile = "package-lock.json"

      FileFunction.cached(
        streams.value.cacheDirectory /
          "vite" /
          (if (configuration.value == Compile) "main" else "test"),
        inStyle = FilesInfo.hash
      ) { filesToCopy =>
        filesToCopy
          .filter(_.exists())
          .foreach(file => IO.copyFile(file, targetDir / file.getName))

        ScalaProcess(cmd("npm") ::: "install" :: Nil, targetDir)
          .run(eagerLogger(s.log))
          .exitValue()

        IO.copyFile(
          targetDir / lockFile,
          baseDirectory.value / "vite" / lockFile
        )

        Set.empty
      }(
        Set(baseDirectory.value / "vite" / lockFile)
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

        ScalaProcess(cmd("npm") ::: "run" :: "build" :: Nil, targetDir)
          .run(eagerLogger(logger))
          .exitValue()
      }
      // TODO figure out what makes sense here, might need to run viteBuild instead of just compile
      // (Compile / compile) := ((Compile / compile) dependsOn viteCompile).value
    ) ++ viteTask(
      stageTask,
      startVite,
      stopVite,
      "dev"
    ) ++ viteTask(
      stageTask,
      startVitePreview,
      stopVitePreview,
      "preview"
    )
  }
}
