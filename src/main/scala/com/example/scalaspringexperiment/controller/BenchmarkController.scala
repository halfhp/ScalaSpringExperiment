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
   * @param waitIntervalMs
   * @param parallelism
   * @return
   */
  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/benchmark"))
  def benchmark(
    @RequestParam(defaultValue = "1") count: Int,
    @RequestParam(defaultValue = "10") durationMs: Long,
    @RequestParam(defaultValue = "0") waitIntervalMs: Long,
    @RequestParam(defaultValue = "1") parallelism: Int
  ): Mono[ResponseEntity[Json]] = helper.maybeAuth { _ =>
    benchmarkService.doCpuIntensiveThings(
      iterations = count,
      iterationDurationMs = durationMs,
      waitIntervalMs = waitIntervalMs,
      parallelism = parallelism
    ).map { result =>
      ResponseEntity.ok(Json.obj(
        "result" -> result.asJson
      ))
    }
  }
}
