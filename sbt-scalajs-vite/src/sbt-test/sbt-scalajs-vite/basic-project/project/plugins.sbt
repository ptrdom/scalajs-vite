//val scalaJSViteVersion = sys.props.getOrElse(
//  "plugin.version",
//  sys.error("'plugin.version' environment variable is not set")
//)
//addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)
lazy val root = (project in file(".")).dependsOn(plugin)
lazy val plugin = ProjectRef(
  file("C:\\Users\\Domantas\\IdeaProjects\\open-source\\scalajs-vite"),
  "sbt-scalajs-vite"
)

libraryDependencies += "org.scalatestplus" %% "selenium-4-4" % "3.2.14.0"
libraryDependencies += "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.14"
