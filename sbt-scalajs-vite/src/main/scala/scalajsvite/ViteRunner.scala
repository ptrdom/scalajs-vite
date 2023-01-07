package scalajsvite

import sbt.File
import sbt.util.Logger
import scalajsvite.Logging.eagerLogger

import scala.sys.process.Process

trait ViteRunner {
  def process(logger: Logger)(command: String, directory: File): Process
  def run(logger: Logger)(command: String, directory: File): Unit
}

object ViteRunner {
  object Default extends ViteRunner {
    override def process(
        logger: Logger
    )(command: String, directory: File): Process = {
      Process(prepareCommand(command), directory)
        .run(eagerLogger(logger))
    }

    override def run(logger: Logger)(command: String, directory: File): Unit = {
      val exitValue = Process(prepareCommand(command), directory)
        .run(eagerLogger(logger))
        .exitValue()
      if (exitValue != 0) {
        sys.error(s"Nonzero exit value: $exitValue")
      } else ()
    }

    private def prepareCommand(command: String) = {
      List("node", "node_modules/vite/bin/vite") ::: command :: Nil
    }
  }
}
