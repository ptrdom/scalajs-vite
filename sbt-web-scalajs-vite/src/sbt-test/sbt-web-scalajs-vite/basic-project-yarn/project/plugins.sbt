val scalaJSViteVersion = sys.props.getOrElse(
  "plugin.version",
  sys.error("'plugin.version' environment variable is not set")
)
addSbtPlugin("sbt-scalajs-vite" % "sbt-scalajs-vite" % scalaJSViteVersion)
addSbtPlugin(
  "sbt-web-scalajs-vite" % "sbt-web-scalajs-vite" % scalaJSViteVersion
)

libraryDependencies += "org.scalatestplus" %% "selenium-4-4" % "3.2.14.0"
libraryDependencies += "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.14"
