package example.facade.reactrouter

import slinky.core.ExternalComponent
import slinky.core.ReactComponentClass
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.readwrite.Reader

import scala.scalajs.js
import scala.scalajs.js.UndefOr

@react object Route extends ExternalComponent {

  def renderFunc(f: RouteProps => ReactElement)(implicit r: Reader[RouteProps]): js.Function =
    (obj: js.Object) => f(r.read(obj))

  def withRender(
      path: String,
      render: RouteProps => ReactElement,
      exact: Boolean = false
  ): ReactElement = {
    this.apply(Props(path, renderFunc(render), js.undefined, exact))
  }

  def withComponent(
      path: String,
      component: ReactComponentClass[_],
      exact: Boolean = false
  ): ReactElement = {
    this.apply(Props(path, js.undefined, component, exact))
  }

  case class Props(
      path: String,
      render: UndefOr[js.Function],
      component: UndefOr[ReactComponentClass[_]],
      exact: UndefOr[Boolean]
  )

  override val component = ReactRouterDom.Route

}
