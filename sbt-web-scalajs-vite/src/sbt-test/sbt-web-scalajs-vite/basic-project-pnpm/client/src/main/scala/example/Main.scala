package example

import org.scalajs.dom.Fetch
import org.scalajs.dom.document
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.scalajs.js.Thenable.Implicits._

object Main extends App {
  Fetch
    .fetch("/hello")
    .flatMap(_.text())
    .foreach { response =>
      document.querySelector("#app").innerHTML = s"""
           |  <h1>
           |    $response
           |  </h1>
           |""".stripMargin
    }
}
