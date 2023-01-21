package example

import java.io.File

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.selenium.WebBrowser

class Test
    extends AnyFreeSpec
    with Matchers
    with WebBrowser
    with BeforeAndAfterAll
    with Eventually {

  // TODO provide with sbt, maybe by javaOptions?
  val viteTargetDirectory =
    "C:\\Users\\Domantas\\IdeaProjects\\open-source\\scalajs-vite\\sbt-scalajs-vite\\src\\sbt-test" +
      "\\sbt-scalajs-vite\\electron-project\\target\\scala-2.13\\vite\\main"

  val chromeOptions: ChromeOptions = {
    val value = new ChromeOptions
    value.setBinary(
      new File(
        viteTargetDirectory + "\\release\\0.0.0\\win-unpacked\\electron-project.exe"
      )
    )
    value
  }

  implicit val webDriver: WebDriver = new ChromeDriver(
    new ChromeDriverService.Builder()
      .usingDriverExecutable(
        new File(
          viteTargetDirectory + "\\node_modules\\electron-chromedriver\\bin\\chromedriver.exe"
        )
      )
      .usingAnyFreePort()
      .build(),
    chromeOptions
  )

  override protected def afterAll(): Unit = {
    webDriver.quit()
    super.afterAll()
  }

  "Test" in {
    eventually {
      find(tagName("h1")).head.text shouldBe "Hello World!"
    }
  }
}
