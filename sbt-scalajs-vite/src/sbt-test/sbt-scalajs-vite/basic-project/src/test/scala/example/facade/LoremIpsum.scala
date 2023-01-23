package example.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("lorem-ipsum", JSImport.Namespace)
@js.native
object LoremIpsum extends js.Object {
  def loremIpsum(): String = js.native
}
