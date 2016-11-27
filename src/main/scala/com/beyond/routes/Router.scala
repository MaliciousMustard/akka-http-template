package com.beyond.routes

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext

@Singleton
class Router @Inject()()(implicit ec: ExecutionContext) {

  val route: Route =
    path("ruok") {
      get {
        complete(StatusCodes.OK, "YES")
      }
    }
}
