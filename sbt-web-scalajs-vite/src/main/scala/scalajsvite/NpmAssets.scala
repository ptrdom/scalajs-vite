package scalajsvite

import com.typesafe.sbt.web.PathMapping
import sbt.*
import sbt.Keys.crossTarget
import scalajsvite.ScalaJSVitePlugin.autoImport.viteInstall

object NpmAssets {

  def ofProject(
      project: ProjectReference
  )(assets: File => PathFinder): Def.Initialize[Task[Seq[PathMapping]]] =
    Def.task {
      val nodeModules =
        (project / Compile / viteInstall / crossTarget).value / "node_modules"
      assets(nodeModules).pair(Path.relativeTo(nodeModules))
    }

}
