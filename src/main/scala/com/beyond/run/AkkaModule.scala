package com.beyond.run

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object AkkaModule extends AbstractModule {
  @Provides
  @Singleton
  def provideActorSystem(config: Config, injector: Injector): ActorSystem = ActorSystem("main-actor-system", config)

  @Provides
  def provideExecutionContext(actorSystem: ActorSystem): ExecutionContext = actorSystem.dispatcher

  @Provides
  def provideActorMaterializer(actorSystem: ActorSystem): ActorMaterializer = ActorMaterializer()(actorSystem)

  override def configure(): Unit = {}
}
