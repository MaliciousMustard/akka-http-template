package com.beyond.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class RouterSpec extends FlatSpec with Matchers with MockitoSugar with ScalatestRouteTest {

  implicit val ec = ExecutionContext.global

  val router = new Router

  it should "respond with 200 to an ruok query" in {
    Get("/ruok") ~> router.route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "YES"
    }
  }
}
