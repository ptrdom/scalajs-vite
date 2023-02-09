package example

import org.scalajs.dom.document

object Main extends App {
  document.querySelector("#app").innerHTML = s"""
      |  <h1>
      |    basic-project-pnpm works!
      |  </h1>
      |""".stripMargin
}
