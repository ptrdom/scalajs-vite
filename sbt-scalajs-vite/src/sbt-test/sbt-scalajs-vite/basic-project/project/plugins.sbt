val sourcePlugins = if (!sys.props.contains("plugin.source")) {
  Seq.empty
} else {
  Seq(
    ProjectRef(
      file("../../../../../../"),
      "sbt-scalajs-vite"
    ): ClasspathDep[ProjectReference]
  )
}

lazy val root = (project in file("."))
  .dependsOn(sourcePlugins: _*)

if (sourcePlugins.nonEmpty) {
  Seq.empty
} else {
  val scalaJSViteVersion = sys.props.getOrElse(
    "plugin.version",
    sys.error("'plugin.version' environment variable is not set")
  )
  Seq(
    addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)
  )
}

libraryDependencies += "org.scalatestplus" %% "selenium-4-9" % "3.2.16.0"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "4.12.1"
libraryDependencies += "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.16"
