package scalajsvite

import sbt.util.Logger

import scala.sys.process.ProcessLogger

private[scalajsvite] object Logging {
  def eagerLogger(log: Logger) = {
    ProcessLogger(
      out => log.info(out),
      err => log.error(err)
    )
  }
}
