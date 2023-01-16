package example.facade.node

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

@js.native
@JSGlobalScope
object NodeGlobals extends js.Object {
  val process: Process = js.native
  val __dirname: String = js.native
}

@js.native
trait Process extends js.Object {
  val platform: String = js.native
  val versions: js.Dictionary[String] = js.native
  val env: js.Dynamic = js.native
}
