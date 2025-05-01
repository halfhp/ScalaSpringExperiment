package com.example.scalaspringexperiment.controller

import cats.effect.IO
import com.example.scalaspringexperiment.service.BenchmarkService
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, RequestParam, RestController}
import reactor.core.publisher.Mono

@RestController
class BenchmarkController(
  benchmarkService: BenchmarkService,
  helper: ControllerHelper
) {

  /**
   * An endpoint to help with load testing; performs configurable cpu-intensive work
   * that can be invoked via a load testing tool like JMeter.
   *
   * @param count
   * @param durationMs
   * @param parallelism
   * @return
   */
  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/benchmark"))
  def benchmark(
    @RequestParam(defaultValue = "1") count: Int,
    @RequestParam(defaultValue = "10") durationMs: Long,
    @RequestParam(defaultValue = "1") parallelism: Int
  ): Mono[ResponseEntity[Json]] = helper.maybeAuth { _ =>
    parallelism match {
      case 1 =>
        benchmarkService.doCpuIntensiveThingsSerially(
          count = count,
          individualDurationMs = durationMs
        ).map { result =>
          ResponseEntity.ok(Json.obj(
            "result" -> result.asJson
          ))
        }
      case n if n > 1 =>
        benchmarkService.doCpuIntensiveThingsInParallel(
          count = count,
          individualDurationMs = durationMs,
          parallelism = n
        ).map { result =>
          ResponseEntity.ok(Json.obj(
            "result" -> result.asJson
          ))
        }
      case _ =>
        IO(ResponseEntity.badRequest().body(Json.obj("error" -> Json.fromString("Invalid mode"))))
    }
  }
}
