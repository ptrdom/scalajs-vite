package example.facade.node

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

@js.native
@JSGlobalScope
object NodeGlobals extends js.Object {
  val process: Process = js.native
}

@js.native
trait Process extends js.Object {
  val env: js.Dynamic = js.native
}
