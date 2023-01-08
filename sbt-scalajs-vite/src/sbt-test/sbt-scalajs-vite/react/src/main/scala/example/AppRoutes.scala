package example

import example.App.Props
import example.facade.reactrouter.RouteProps
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Fragment

@react object AppRoutes {
  type Props = RouteProps
  val component: FunctionalComponent[Props] = FunctionalComponent { _ =>
    App()
  }
}
