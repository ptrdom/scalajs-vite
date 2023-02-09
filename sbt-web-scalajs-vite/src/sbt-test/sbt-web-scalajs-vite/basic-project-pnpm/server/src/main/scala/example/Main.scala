package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.util.Failure
import scala.util.Success

object Main extends App {
  implicit val system = ActorSystem(Behaviors.empty, "main-system")
  implicit val executionContext = system.executionContext

  val route = concat(
    pathSingleSlash {
      get {
        encodeResponse {
          getFromResource(s"index.html")
        }
      }
    },
    path("hello") {
      get {
        complete(
          HttpEntity(ContentTypes.`text/plain(UTF-8)`, "basic-project-pnpm-sbt-web works!")
        )
      }
    },
    path("favicon.ico") {
      complete("")
    },
    path(Remaining) { file =>
      encodeResponse {
        getFromResource(file)
      }
    }
  )

  val binding = Http().newServerAt("0.0.0.0", args.headOption.map(_.toInt).getOrElse(sys.error("Missing port argument"))).bind(route)

  binding.onComplete {
    case Success(binding) =>
      println(
        s"HTTP server bound to: http://localhost:${binding.localAddress.getPort}"
      )
    case Failure(ex) =>
      println(s"HTTP server binding failed: ${ex.getMessage}")
      system.terminate()
  }
}
