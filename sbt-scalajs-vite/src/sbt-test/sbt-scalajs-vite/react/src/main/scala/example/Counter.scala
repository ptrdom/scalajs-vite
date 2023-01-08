package example

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.useState
import slinky.web.html.button
import slinky.web.html.div
import slinky.web.html.onClick
import slinky.web.html.p

@react object Counter {
  type Props = Unit
  val component: FunctionalComponent[Props] = FunctionalComponent { _ =>
    val (counter, setCounter) = useState(0)
    div(
      p(s"counter value: $counter"),
      button(onClick := (() => setCounter(counterValue => counterValue + 1)))(
        "increment counter"
      )
    )
  }
}
