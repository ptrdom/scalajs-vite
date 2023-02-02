val scalaJSViteVersion = sys.props.getOrElse(
  "plugin.version",
  sys.error("'plugin.version' environment variable is not set")
)
addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)
