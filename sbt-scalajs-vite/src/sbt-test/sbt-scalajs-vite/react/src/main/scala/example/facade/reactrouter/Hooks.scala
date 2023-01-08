package example.facade.reactrouter

import slinky.readwrite.Reader

object Hooks {
  def useParams[T]()(implicit r: Reader[T]): T = {
    val params = ReactRouterDom.useParams()
    r.read(params)
  }

  def useHistory()(implicit r: Reader[History]): History = {
    val history = ReactRouterDom.useHistory()
    r.read(history)
  }
}
