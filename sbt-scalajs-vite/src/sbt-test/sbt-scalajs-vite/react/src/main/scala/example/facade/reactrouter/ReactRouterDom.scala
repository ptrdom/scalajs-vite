package example.facade.reactrouter

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("react-router-dom", JSImport.Namespace)
@js.native
object ReactRouterDom extends js.Object {
  val BrowserRouter: js.Object = js.native
  val Route: js.Object = js.native
  val Link: js.Object = js.native
  val Redirect: js.Object = js.native

  def useHistory(): js.Object = js.native
  def useParams(): js.Object = js.native
}
