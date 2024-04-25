package example

import org.scalajs.dom
import org.scalajs.dom.document

object Main extends App {
  document.addEventListener(
    "DOMContentLoaded",
    { (_: dom.Event) =>
      setupUI()
    }
  )

  def setupUI(): Unit = {
    document.querySelector("#app").innerHTML = "<h1>basic-project works!</h1>".stripMargin
  }
}
