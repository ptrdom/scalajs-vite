package example

import example.facade.node._
import example.facade.playwright._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits._

// https://playwright.dev/docs/api/class-electron rewritten in Scala.js
class PlaywrightSpec extends AsyncWordSpec with Matchers {

  implicit override def executionContext =
    JSExecutionContext.queue

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
        _ <- window.screenshot(new ScreenshotConfig {
          override val path = "intro.png"
        })
        _ <- electronApp.close()
      } yield succeed
    }
  }
}
