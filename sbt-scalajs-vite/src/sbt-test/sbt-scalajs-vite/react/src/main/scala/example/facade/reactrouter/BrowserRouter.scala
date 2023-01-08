package example.facade.reactrouter

import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.UndefOr

@react object BrowserRouter extends ExternalComponent {
  case class Props(
      basename: UndefOr[String] = js.undefined,
      getUserConfirmation: UndefOr[js.Function] = js.undefined,
      forceRefresh: UndefOr[Boolean] = js.undefined,
      keyLength: UndefOr[Int] = js.undefined
  )

  override val component = ReactRouterDom.BrowserRouter
}
