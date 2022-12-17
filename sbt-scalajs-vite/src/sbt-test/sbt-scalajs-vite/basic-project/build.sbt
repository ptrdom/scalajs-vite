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
