package scalajsvite

import sbt.File
import sbt.internal.util.ManagedLogger
import sbt.util.Logger

import scala.sys.process.{Process => ScalaProcess}
import scala.sys.process.ProcessLogger

trait PackageManager {
  import PackageManager._

  def name: String
  def lockFile: String

  def cmd: List[String] = sys.props("os.name").toLowerCase match {
    case os if os.contains("win") => "cmd" :: "/c" :: name :: Nil
    case _                        => name :: Nil
  }

  def installCommand: String
  def install(logger: Logger)(directory: File): Unit = {
    ScalaProcess(cmd ::: installCommand :: Nil, directory)
      .run(eagerLogger(logger))
      .exitValue()
  }

  def runCommand: String
  def runJava(
      logger: Logger
  )(command: String, directory: File): (Process, Thread, Thread) = {
    // using Java Process to use `descendants`
    val pb =
      new ProcessBuilder(cmd ::: runCommand :: command :: Nil: _*)
    pb.directory(directory)
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
    (p, stdoutThread, stderrThread)
  }

  def build(logger: Logger)(directory: File): Unit = {
    ScalaProcess(cmd ::: runCommand :: "build" :: Nil, directory)
      .run(eagerLogger(logger))
      .exitValue()
  }
}

object PackageManager {
  private def eagerLogger(log: Logger) = {
    ProcessLogger(
      out => log.info(out),
      err => log.error(err)
    )
  }

  object Npm extends PackageManager {
    override def name: String = "npm"
    override def lockFile: String = "package-lock.json"
    override def installCommand: String = "install"
    override def runCommand: String = "run"
  }
}
