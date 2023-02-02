package example

import example.facade.node.NodeGlobals.process
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.window

import scala.scalajs.js

/** The preload script runs before. It has access to web APIs as well as
  * Electron's renderer process modules and some polyfilled Node.js functions.
  *
  * https://www.electronjs.org/docs/latest/tutorial/sandbox
  */
object Preload extends App {
  window.addEventListener(
    "DOMContentLoaded",
    (_ => {
      val replaceText = (selector: String, text: String) => {
        val element = document.getElementById(selector)
        if (element != null) element.innerText = text
      }

      js.Array("chrome", "node", "electron")
        .foreach(`type` =>
          replaceText(
            s"${`type`}-version",
            process.versions.get(`type`).orNull
          )
        )
    }): js.Function1[Event, Unit]
  )
}
