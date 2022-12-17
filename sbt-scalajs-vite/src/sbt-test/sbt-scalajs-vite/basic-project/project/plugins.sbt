// TODO convert to sys.props.getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set"))
val scalaJSViteVersion = "0.1.0-SNAPSHOT"
addSbtPlugin("sbt-scalajs-vite" % "sbt-scalajs-vite" % scalaJSViteVersion)
