package com.example.scalaspringexperiment.controller

import io.circe.Json
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.{ExceptionHandler, RestControllerAdvice}
import org.springframework.web.server.{ResponseStatusException, ServerWebExchange}
import reactor.core.publisher.Mono

@RestControllerAdvice
class ControllerErrorHandler {

  @ExceptionHandler(Array(classOf[Throwable]))
  def handleError(exchange: ServerWebExchange, ex: Throwable): Mono[Json] = {
    val path = exchange.getRequest.getPath.toString
    val status = ex match {
      case e: ResponseStatusException => e.getStatusCode.value()
      case _                          => HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    val errorJson = Json.obj(
      "error" -> Json.fromString(ex.getMessage),
      "path" -> Json.fromString(path),
      "status" -> Json.fromInt(status)
    )

    Mono.just(errorJson)
  }
}
