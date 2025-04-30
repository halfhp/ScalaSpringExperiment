package com.example.scalaspringexperiment.service

import cats.syntax.traverse.*
import cats.effect.IO
import cats.effect.implicits.concurrentParTraverseOps
import org.springframework.stereotype.Service


case class BenchmarkResult(
  totalDurationMs: Long,
  individualDurationMs: Long,
  iterations: Long
)

/**
 * Provides methods to aid in benchmarking the performance of the application.
 */
@Service
class BenchmarkService {

def doCpuIntensiveThingsSerially(
  count: Int,
  individualDurationMs: Long,
): IO[BenchmarkResult] = {
  val start = System.currentTimeMillis()
  (1 to count).toList.traverse { _ =>
    doSomethingCpuIntensive(individualDurationMs)
  }.flatMap { _ =>
    val totalDuration = System.currentTimeMillis() - start
    IO.pure(
    BenchmarkResult(
      totalDurationMs = totalDuration,
      individualDurationMs = individualDurationMs,
      iterations = count
    ))
  }
}

  def doCpuIntensiveThingsInParallel(
    count: Int,
    individualDurationMs: Long,
    parallelism: Int
  ): IO[BenchmarkResult] = {
    val start = System.currentTimeMillis()
    (1 to count).toList.parTraverseN(parallelism) { _ =>
      doSomethingCpuIntensive(individualDurationMs)
    }.flatMap { _ =>
      val totalDuration = System.currentTimeMillis() - start
      IO.pure(
        BenchmarkResult(
          totalDurationMs = totalDuration,
          individualDurationMs = individualDurationMs,
          iterations = count
        )
      )
    }
  }

  def doSomethingCpuIntensive(
    durationMs: Long
  ): IO[Long] = IO {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < durationMs) {
      // Simulate CPU-intensive work
      Math.sqrt(Math.random())
    }
    System.currentTimeMillis() - start
  }

}
