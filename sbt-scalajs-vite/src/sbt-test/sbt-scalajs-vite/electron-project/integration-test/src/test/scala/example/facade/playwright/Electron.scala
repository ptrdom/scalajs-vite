package example.facade.playwright

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("playwright", "_electron")
object Electron extends js.Object {
  def launch(config: LaunchConfig): js.Promise[ElectronApplication] = js.native
}

trait LaunchConfig extends js.Object {
  val args: js.Array[String]
}

@js.native
trait ElectronApplication extends js.Object {

  def firstWindow(): js.Promise[Window] = js.native

  def close(): js.Promise[Unit] = js.native
}

@js.native
trait Window extends js.Object {
  def title(): js.Promise[String] = js.native

  def screenshot(config: ScreenshotConfig): js.Promise[js.Dynamic]
}

trait ScreenshotConfig extends js.Object {
  val path: String
}
