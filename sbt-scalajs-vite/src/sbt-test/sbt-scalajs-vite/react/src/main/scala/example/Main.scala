package example

import example.facade.reactrouter.BrowserRouter
import example.facade.reactrouter.withRouter
import org.scalajs.dom.document
import slinky.core.facade.StrictMode
import slinky.web.ReactDOM

object Main extends App {
  ReactDOM.render(
    StrictMode(BrowserRouter(withRouter(AppRoutes.component))),
    document.querySelector("#app")
  )
}
