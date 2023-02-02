import org.scalajs.linker.interface.ModuleInitializer
import org.scalajs.sbtplugin.Stage
import scala.sys.process._

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

// Suppress meaningless 'multiple main classes detected' warning
Compile / mainClass := None

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"

val viteElectronBuildPackage =
  taskKey[Unit]("Generate package directory with electron-builder")
val viteElectronBuildDistributable =
  taskKey[Unit]("Package in distributable format with electron-builder")

def viteElectronBuildTask(): Seq[Setting[_]] = {
  Seq(Compile, Test)
    .flatMap { config =>
      inConfig(config)(
        Seq(Stage.FastOpt, Stage.FullOpt).flatMap { stage =>
          val stageTask = stage match {
            case Stage.FastOpt => fastLinkJS
            case Stage.FullOpt => fullLinkJS
          }

          def fn(args: List[String] = Nil) = Def.task {
            val log = streams.value.log

            (stageTask / viteBuild).value

            val targetDir = (viteInstall / crossTarget).value

            val exitValue = Process(
              "node" :: "node_modules/electron-builder/cli" :: Nil ::: args,
              targetDir
            ).run(
              ProcessLogger(
                out => log.info(out),
                err => log.error(err)
              )
            ).exitValue()
            if (exitValue != 0) {
              sys.error(s"Nonzero exit value: $exitValue")
            } else ()
          }

          Seq(
            stageTask / viteElectronBuildPackage := fn("--dir" :: Nil).value,
            stageTask / viteElectronBuildDistributable := fn().value
          )
        }
      )
    }
}

viteElectronBuildTask()

InputKey[Unit]("html") := {
  import org.openqa.selenium.WebDriver
  import org.openqa.selenium.chrome.ChromeDriver
  import org.openqa.selenium.chrome.ChromeOptions
  import org.scalatest.matchers.should.Matchers
  import org.scalatestplus.selenium.WebBrowser
  import org.scalatest.concurrent.Eventually
  import org.scalatest.concurrent.IntegrationPatience

  val ags = Def.spaceDelimited().parsed.toList

  val port =
    ags match {
      case List(port) => port
      case _          => sys.error("missing arguments")
    }

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
    go to s"http://localhost:$port"
  }

  eventually {
    find(tagName("h1")).head.text shouldBe "Hello World!"
  }

  ()
}
