package scalajsvite

import java.nio.file.Files

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.Stage
import sbt.Keys._
import sbt._
import sbt.Keys.resourceDirectories
import scalajsvite.ScalaJSVitePlugin.autoImport.viteBuild
import scalajsvite.ScalaJSVitePlugin.autoImport.viteInstall
import webscalajs.WebScalaJS
import webscalajs.WebScalaJS.autoImport._

import scala.jdk.CollectionConverters._

object WebScalaJSVitePlugin extends AutoPlugin {

  override lazy val requires = WebScalaJS

  override lazy val projectSettings = Seq(
    monitoredScalaJSDirectories ++= allFrontendProjectResourceDirectories.value,
    scalaJSPipeline := pipelineStage.value
  )

  val allFrontendProjectResourceDirectories: Def.Initialize[Seq[File]] =
    Def.settingDyn {
      val projectRefs = scalaJSProjects.value.map(Project.projectToRef)
      projectRefs
        .map { project =>
          Def.setting {
            (project / Compile / resourceDirectories).value
          }
        }
        .foldLeft(Def.setting(Seq.empty[File]))((acc, resourceDirectories) =>
          Def.setting(acc.value ++ resourceDirectories.value)
        )
    }

  val bundlesWithSourceMaps: Def.Initialize[Task[Seq[(File, String)]]] =
    Def.settingDyn {
      val projects = scalaJSProjects.value.map(Project.projectToRef)
      Def.task {
        val bundles: Seq[(File, String)] =
          projects
            .map { project =>
              Def.settingDyn {
                val sjsStage = (project / scalaJSStage).value match {
                  case Stage.FastOpt => fastLinkJS
                  case Stage.FullOpt => fullLinkJS
                }
                Def.task {
                  (project / Compile / sjsStage / viteBuild).value
                  val clientTarget =
                    (project / Compile / viteInstall / crossTarget).value
                  val dist = clientTarget / "dist"

                  val viteBuildFiles = Files
                    .find(
                      dist.toPath,
                      Int.MaxValue,
                      (_, _) => true
                    )
                    .iterator()
                    .asScala
                    .map(_.toFile)
                    .toSeq
                  viteBuildFiles.pair(Path.relativeTo(dist))
                }
              }
            }
            .foldLeft(Def.task(Seq.empty[(File, String)]))((acc, bundleFiles) =>
              Def.task(acc.value ++ bundleFiles.value)
            )
            .value

        bundles.flatMap { case (file, path) =>
          val sourceMapFile = file.getParentFile / (file.name ++ ".map")
          if (sourceMapFile.exists) {
            val sourceMapPath = path ++ ".map"
            Seq(file -> path, sourceMapFile -> sourceMapPath)
          } else Seq(file -> path)
        }
      }
    }

  val pipelineStage: Def.Initialize[Task[Pipeline.Stage]] =
    Def.taskDyn {
      val include = (scalaJSPipeline / includeFilter).value
      val exclude = (scalaJSPipeline / excludeFilter).value
      val bundleMappings = bundlesWithSourceMaps.value
      val sourcemapScalaFiles = WebScalaJS.sourcemapScalaFiles.value
      Def.task { mappings: Seq[PathMapping] =>
        val filtered = filterMappings(mappings, include, exclude)

        filtered ++ bundleMappings ++ sourcemapScalaFiles
      }
    }

  def filterMappings(
      mappings: Seq[PathMapping],
      include: FileFilter,
      exclude: FileFilter
  ): Seq[PathMapping] =
    for {
      (file, path) <- mappings
      if include.accept(file) && !exclude.accept(file)
    } yield file -> path

}
