package example

import org.scalajs.dom.document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MainSpec extends AnyWordSpec with Matchers {

  "Main" should {
    "work" in {
      val appContainer = document.createElement("div")
      appContainer.id = "app"
      document.body.append(appContainer)

      Main.setupUI()

      document
        .querySelector("h1")
        .textContent shouldEqual "basic-project works!"
    }
  }
}
