package example

import example.facade.node._
import example.facade.playwright._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout
import scala.scalajs.js.Thenable.Implicits._

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

// https://playwright.dev/docs/api/class-electron rewritten in Scala.js
class PlaywrightSpec extends AsyncWordSpec with Matchers {

  implicit override def executionContext: ExecutionContext = MacrotaskExecutor

  "Main" should {
    "work" in {
      for {
        electronApp <- Electron
          .launch(new LaunchConfig {
            override val args =
              js.Array(NodeGlobals.process.env.MAIN_PATH.asInstanceOf[String])
          })
        window <- electronApp.firstWindow()
        title <- window.title()
        _ = println(s"$title")
        // taking a screenshot became flaky since Electron 35 with xvfb, simplest workaround is to retry until timeout
        // FIXME figure out how to not need this workaround
        _ <- eventually(window.screenshot(new ScreenshotConfig {
          override val path = "intro.png"
        }))
        _ <- electronApp.close()
      } yield succeed
    }
  }

  def eventually[T](f: => Future[T]) = {
    import scala.concurrent.duration._
    val timeout = 15.seconds
    val interval = 150.millis
    val start = System.nanoTime()

    val promise = Promise[Unit]()

    def tryF(): Unit = {
      f
        .onComplete {
          case Success(_) =>
            promise.success(())
          case Failure(ex) =>
            val now = System.nanoTime()
            if ((now - start).nanos < timeout) {
              println(s"Retrying in [$interval]")
              setTimeout(interval) {
                tryF()
              }
            } else {
              promise.failure(
                new RuntimeException(s"Timeout of [$timeout] reached", ex)
              )
            }
        }
    }
    tryF()

    promise.future
  }
}
