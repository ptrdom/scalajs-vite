import org.scalajs.linker.interface.ModuleInitializer

enablePlugins(ScalaJSVitePlugin)

name := "basicProject"
scalaVersion := "2.13.8"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"

scalaJSModuleInitializers := Seq(
  ModuleInitializer
    .mainMethodWithArgs("example.Main", "main")
    .withModuleID("main")
)

TaskKey[Unit]("html") := {
  import org.openqa.selenium.WebDriver
  import org.openqa.selenium.chrome.ChromeDriver
  import org.openqa.selenium.chrome.ChromeOptions
  import org.scalatest.matchers.should.Matchers
  import org.scalatestplus.selenium.WebBrowser
  import org.scalatest.concurrent.Eventually
  import org.scalatest.concurrent.IntegrationPatience

  val webBrowser = new WebBrowser
    with Matchers
    with Eventually
    with IntegrationPatience {
    val chromeOptions: ChromeOptions = {
      val value = new ChromeOptions
      // arguments recommended by https://itnext.io/how-to-run-a-headless-chrome-browser-in-selenium-webdriver-c5521bc12bf0
      value.addArguments(
        "--disable-gpu",
        "--window-size=1920,1200",
        "--ignore-certificate-errors",
        "--disable-extensions",
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--headless"
      )
      value
    }
    implicit val webDriver: WebDriver = new ChromeDriver(chromeOptions)
  }
  import webBrowser._

  eventually {
    go to "http://localhost:5173"
    find(tagName("h1")).head.text shouldBe "basic-project works!"
  }

  ()
}
