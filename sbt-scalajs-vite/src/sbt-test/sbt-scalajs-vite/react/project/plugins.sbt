//TODO remove once implemented
resolvers +=
  "Sonatype OSS1 Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
val scalaJSViteVersion = "0.0.0+14-570fca60+20230102-1511-SNAPSHOT"
//TODO uncomment once implemented
//val scalaJSViteVersion = sys.props.getOrElse(
//  "plugin.version",
//  sys.error("'plugin.version' environment variable is not set")
//)
addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)

libraryDependencies += "org.scalatestplus" %% "selenium-4-4" % "3.2.14.0"
libraryDependencies += "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.14"
