resolvers +=
  "Sonatype OSS1 Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
val scalaJSViteVersion = "0.0.0+17-a42bd251-SNAPSHOT"
//val scalaJSViteVersion = sys.props.getOrElse(
//  "plugin.version",
//  sys.error("'plugin.version' environment variable is not set")
//)
addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)

//libraryDependencies += "org.scalatestplus" %% "selenium-4-4" % "3.2.14.0"
//libraryDependencies += "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.14"
