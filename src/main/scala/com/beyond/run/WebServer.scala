package com.beyond.run

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.beyond.routes.Logger.logRequests
import com.beyond.routes.Router
import com.google.inject.{Guice, Injector}
import com.typesafe.config.Config
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext
object WebServer extends App {

  val injector: Injector = Guice.createInjector(ConfigModule, AkkaModule)

  implicit val (system, materializer, ec) = (
    injector.instance[ActorSystem],
    injector.instance[ActorMaterializer],
    injector.instance[ExecutionContext]
    )

  val (config, router) = (
    injector.instance[Config],
    injector.instance[Router]
    )

  val bindingFuture = Http().bindAndHandle(
    logRequests(router.route),
    config.getString("http.interface"),
    config.getInt("http.port")
    )

  println("Startup complete.")
}

