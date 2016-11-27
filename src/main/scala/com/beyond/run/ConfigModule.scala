package com.beyond.run

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import net.codingwell.scalaguice.ScalaModule

object ConfigModule extends AbstractModule with ScalaModule {

  @Provides
  @Singleton
  def provideConfig(): Config = ConfigFactory.load()

  override def configure(): Unit = {}
}
