import org.scalajs.linker.interface.ModuleInitializer

enablePlugins(ScalaJSVitePlugin)

scalaVersion := "2.13.8"

scalaJSModuleInitializers := Seq(
  ModuleInitializer
    .mainMethodWithArgs("example.Main", "main")
    .withModuleID("main"),
  ModuleInitializer
    .mainMethodWithArgs("example.Preload", "main")
    .withModuleID("preload"),
  ModuleInitializer
    .mainMethodWithArgs("example.Renderer", "main")
    .withModuleID("renderer")
)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"

//InputKey[Unit]("html") := {
//  import org.openqa.selenium.WebDriver
//  import org.openqa.selenium.chrome.ChromeDriver
//  import org.openqa.selenium.chrome.ChromeOptions
//  import org.scalatest.matchers.should.Matchers
//  import org.scalatestplus.selenium.WebBrowser
//  import org.scalatest.concurrent.Eventually
//  import org.scalatest.concurrent.IntegrationPatience
//
//  val ags = Def.spaceDelimited().parsed.toList
//
//  val port =
//    ags match {
//      case List(port) => port
//      case _          => sys.error("missing arguments")
//    }
//
//  val webBrowser = new WebBrowser
//    with Matchers
//    with Eventually
//    with IntegrationPatience {
//    val chromeOptions: ChromeOptions = {
//      val value = new ChromeOptions
//      // arguments recommended by https://itnext.io/how-to-run-a-headless-chrome-browser-in-selenium-webdriver-c5521bc12bf0
//      value.addArguments(
//        "--disable-gpu",
//        "--window-size=1920,1200",
//        "--ignore-certificate-errors",
//        "--disable-extensions",
//        "--no-sandbox",
//        "--disable-dev-shm-usage",
//        "--headless"
//      )
//      value
//    }
//    implicit val webDriver: WebDriver = new ChromeDriver(chromeOptions)
//  }
//  import webBrowser._
//
//  eventually {
//    go to s"http://localhost:$port"
//  }
//
//  eventually {
//    find(tagName("h1")).head.text shouldBe "basic-project works!"
//  }
//
//  ()
//}
