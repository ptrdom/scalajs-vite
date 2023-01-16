package example

import example.electron.BrowserWindow
import example.electron.BrowserWindowConfig
import example.electron.ElectronGlobals.app
import example.electron.WebPreferences
import example.facade.node.NodeGlobals.__dirname
import example.facade.node.NodeGlobals.process
import example.facade.node.Path.join

import scala.scalajs.js
import scala.scalajs.js.|

object Main extends App {
  println("Main works")

  val url = process.env.VITE_DEV_SERVER_URL.asInstanceOf[String]

  println(s"__dirname ${__dirname}")

  def createWindow(): Unit = {
    val mainWindow = new BrowserWindow(new BrowserWindowConfig {
      override val height = 600
      override val width = 800
      override val webPreferences = new WebPreferences {
        override val preload = join(__dirname, "preload.js")
      }
    })

    if (url != null) {
      mainWindow.loadURL(url)
    } else {
      mainWindow.loadFile(join(__dirname, "dist", "index.html"))
    }
  }

  app
    .whenReady()
    .`then`((_ => {
      createWindow()

      ()
    }): js.Function1[Unit, Unit | js.Thenable[Unit]])
}
