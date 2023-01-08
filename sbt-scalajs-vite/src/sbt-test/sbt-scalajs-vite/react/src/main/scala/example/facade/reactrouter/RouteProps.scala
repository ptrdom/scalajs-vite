package example.facade.reactrouter

import scala.scalajs.js
import scala.scalajs.js.UndefOr

case class Match(params: js.Object, isExact: Boolean, path: String, url: String)
case class Location(key: UndefOr[String], pathname: UndefOr[String], search: UndefOr[String], hash: UndefOr[String])
case class History(
    length: Int,
    action: String,
    location: Location,
    push: String => Unit,
    replace: String => Unit,
    goBack: () => Unit,
    goForward: () => Unit,
    block: () => Unit
)
case class RouteProps(`match`: Match, location: Location, history: History)
