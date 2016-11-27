package com.beyond.routes

import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, RemoteAddress}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, RejectionHandler, _}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.http.scaladsl.settings.RoutingSettings
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Logger {

  private val formatter = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withZoneUTC()

  private def logRequestWithIpAndTimestamp(loggingAdapter: LoggingAdapter,
                                           ip: RemoteAddress,
                                           requestTimestamp: Long,
                                           level: LogLevel = Logging.InfoLevel)(req: HttpRequest)(res: Any): Unit = {

    val responseTimestamp: Long = System.nanoTime
    val elapsedTime: Long = (responseTimestamp - requestTimestamp) / 1000000

    res match {
      case Complete(resp) =>
        val logIp = ip.toOption.map(_.getHostAddress).getOrElse("0.0.0.0")
        val logTime = formatter.print(DateTime.now())
        val logBytes = resp.entity.contentLengthOption.map(_.toString).getOrElse("-")
        val loggingString = s"""access | $logIp - - [$logTime] ${requestResponseLogSegment(req, resp)} $logBytes | timetaken=$elapsedTime"""
        LogEntry(loggingString, level).logTo(log = loggingAdapter)
      case _ =>
    }
  }

  private def requestResponseLogSegment(request: HttpRequest, response: HttpResponse): String =
    s""""${request.method.value} ${request.uri.toRelative} ${request.protocol.value}" ${response.status.intValue()}"""

  private def logRequestWithIp(ip: RemoteAddress)(log: LoggingAdapter) = {
    val requestTimestamp = System.nanoTime
    logRequestWithIpAndTimestamp(log, ip, requestTimestamp)(_)
  }

  private val logRequest = extractClientIP flatMap { ip =>
    DebuggingDirectives.logRequestResult(LoggingMagnet(logRequestWithIp(ip)(_)))
  }

  // Wrap default exception handler in logging
  private val myExceptionHandler = { settings: RoutingSettings =>
    ExceptionHandler {
      case e => logRequest(ExceptionHandler.default(settings)(e))
    }
  }

  // Wrap default rejection handler in logging
  private val myRejectionHandler = new RejectionHandler {
    def apply(rejections: scala.collection.immutable.Seq[Rejection]) = {
      RejectionHandler.default(rejections).map(logRequest(_))
    }
  }

  // Wrap routes in logging, successful
  val logRequests = { route: Route =>
    extractSettings { settings: RoutingSettings =>
      handleExceptions(myExceptionHandler(settings)){
        handleRejections(myRejectionHandler){
          logRequest(route)
        }
      }
    }
  }

}
