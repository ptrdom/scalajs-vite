package example

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.web.html.div
import slinky.web.html.h1

@react object App {
  type Props = Unit
  val component: FunctionalComponent[Props] = FunctionalComponent { _ =>
    div(
      h1("react-project works!"),
      Counter()
    )
  }
}
