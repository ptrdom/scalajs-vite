package example.facade.electron

import example.facade.node.EventEmitter

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object ElectronGlobals {
  @js.native
  @JSImport("electron", "app")
  def app: Application = js.native
}

@js.native
trait Application extends EventEmitter {
  def whenReady(): js.Promise[Unit]
  def quit(): Unit
}

@js.native
@JSImport("electron", "BrowserWindow")
class BrowserWindow(config: BrowserWindowConfig) extends js.Object {
  def loadURL(url: String): js.Promise[Unit] = js.native
  def loadFile(filePath: String): js.Promise[Unit] = js.native
  def webContents: WebContents = js.native
}

trait BrowserWindowConfig extends js.Object {
  val height: js.UndefOr[Int] = js.undefined
  val width: js.UndefOr[Int] = js.undefined
  val webPreferences: js.UndefOr[WebPreferences] = js.undefined
}

trait WebPreferences extends js.Object {
  val preload: js.UndefOr[String] = js.undefined
  val nodeIntegration: js.UndefOr[Boolean] = js.undefined
}

trait WebContents extends js.Object {
  def openDevTools(): Unit
}

@js.native
@JSImport("electron", "BrowserWindow")
object BrowserWindow extends js.Object {
  def getAllWindows(): js.Array[BrowserWindow] = js.native
}
