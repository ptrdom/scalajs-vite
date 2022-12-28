package scalajsvite

import sbt.File
import sbt.util.Logger
import scalajsvite.Logging.eagerLogger

import scala.sys.process.Process

trait PackageManager {

  def name: String
  def lockFile: String

  def installCommand: String
  def install(logger: Logger)(directory: File): Unit = {
    Process(
      (sys.props("os.name").toLowerCase match {
        case os if os.contains("win") => "cmd" :: "/c" :: Nil
        case _                        => Nil
      }) ::: name :: Nil ::: installCommand :: Nil,
      directory
    )
      .run(eagerLogger(logger))
      .exitValue()
  }
}

object PackageManager {
  object Npm extends PackageManager {
    override def name: String = "npm"
    override def lockFile: String = "package-lock.json"
    override def installCommand: String = "install"
  }
}
