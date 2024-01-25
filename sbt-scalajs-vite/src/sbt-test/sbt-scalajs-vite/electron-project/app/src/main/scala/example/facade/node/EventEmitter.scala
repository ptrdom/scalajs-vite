package example.facade.node

import scala.scalajs.js

@js.native
trait EventEmitter extends js.Object {
  def on(event: String, listener: js.Function): Unit = js.native
}
