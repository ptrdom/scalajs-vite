package example.facade.reactrouter

import slinky.core.ReactComponentClass
import slinky.core.facade.ReactElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("react-router", JSImport.Namespace)
@js.native
object ReactRouter extends js.Object {
  def withRouter: js.Function1[ReactComponentClass[RouteProps], js.Function1[
    js.Object,
    ReactElement
  ]] = js.native
}

//@react object withRouter extends ExternalComponent {
//  case class Props(Component: ReactComponentClass)
//  override val component = ReactRouter.withRouter
//}
