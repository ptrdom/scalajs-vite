import org.scalajs.linker.interface.ModuleInitializer

enablePlugins(ScalaJSVitePlugin)

scalaVersion := "2.13.8"

scalaJSModuleInitializers := Seq(
  ModuleInitializer
    .mainMethodWithArgs("example.Main", "main")
    .withModuleID("main")
)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"

libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.15" % "test"

//Test / jsEnv := new org.scalajs.jsenv.selenium.SeleniumJSEnv(
//  new org.openqa.selenium.chrome.ChromeOptions().addArguments(
//    // recommended options
////    "--headless", // necessary for CI
////    "--disable-gpu",
////    "--window-size=1920,1200",
////    "--ignore-certificate-errors",
////    "--disable-extensions",
////    "--no-sandbox",
////    "--disable-dev-shm-usage",
//    "--disable-web-security" // for CORS
//  ),
//  org.scalajs.jsenv.selenium.SeleniumJSEnv.Config().withKeepAlive(true)
//)

Test / jsEnv := new JSDOMNodeJSEnv(
  JSDOMNodeJSEnv.Config(
    (Test / viteInstall / crossTarget).value
  )
)

Test / jsEnvInput := {
  (Test / fastLinkJS / viteBuild).value
  val assetDirectory =
    (Test / viteInstall / crossTarget).value / "dist" / "assets"
  val bundleFile = assetDirectory
    .list()
    .find(_.startsWith("index"))
    .getOrElse(
      throw new IllegalArgumentException(
        s"Bundle file not found in [${assetDirectory.absolutePath}]"
      )
    )
  Seq(org.scalajs.jsenv.Input.Script((assetDirectory / bundleFile).toPath))
}

scalaJSLinkerConfig ~= {
  _.withModuleSplitStyle(
    org.scalajs.linker.interface.ModuleSplitStyle
      .SmallModulesFor(List("example"))
  )
}

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
