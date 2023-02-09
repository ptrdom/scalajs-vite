package example

import org.scalajs.dom.document

object Main extends App {
  document.querySelector("#app").innerHTML = s"""
      |  <h1>
      |    basic-project-yarn works!
      |  </h1>
      |""".stripMargin
}
