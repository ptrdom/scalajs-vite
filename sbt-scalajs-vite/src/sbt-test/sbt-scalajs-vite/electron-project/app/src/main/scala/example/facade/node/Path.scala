package example.facade.node

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Path {
  @js.native
  @JSImport("path", "join")
  def join(paths: String*): String = js.native
}
