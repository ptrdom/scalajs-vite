package scalajsvite

import sbt.File
import sbt.util.Logger
import scalajsvite.Logging.eagerLogger

import scala.sys.process.Process

trait ViteRunner {
  def process(logger: Logger)(command: String, directory: File): Process
}

object ViteRunner {
  object Default extends ViteRunner {
    override def process(logger: Logger)(command: String, directory: File) = {
      Process(
        List("node", "node_modules/vite/bin/vite") ::: command :: Nil,
        directory
      )
        .run(eagerLogger(logger))
    }
  }
}
