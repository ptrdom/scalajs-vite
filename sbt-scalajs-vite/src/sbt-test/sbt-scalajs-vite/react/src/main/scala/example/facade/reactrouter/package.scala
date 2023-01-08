package example.facade

import slinky.core.BaseComponentWrapper
import slinky.core.ReactComponentClass
import slinky.core.facade.ReactElement
import slinky.readwrite.Reader

import scala.scalajs.js
import scala.scalajs.js.ConstructorTag

package object reactrouter {

  def withRouter(rc: ReactComponentClass[RouteProps]): ReactElement =
    ReactRouter.withRouter(rc)(js.Object())

  def withRouter[T <: BaseComponentWrapper](
      bc: T
  )(implicit
      ev: bc.Props <:< RouteProps,
      propsReader: Reader[bc.Props],
      ctag: ConstructorTag[bc.Def]
  ): ReactElement = {
    val bb = ReactComponentClass
      .wrapperToClass(bc)
      .asInstanceOf[ReactComponentClass[RouteProps]]
    ReactRouter.withRouter(bb)(js.Object())
  }

}
