package example.facade.reactrouter

import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.|

case class To(
    pathname: Option[String] = None,
    search: Option[String] = None,
    hash: Option[String] = None,
    state: Option[js.Object]
)

@react object Link extends ExternalComponent {
  case class Props(
      to: String | To,
      replace: UndefOr[Boolean] = js.undefined,
      innerRef: UndefOr[js.Function] = js.undefined
  )

  override val component = ReactRouterDom.Link

}
