package com.example.scalaspringexperiment.service

import cats.syntax.traverse.*
import cats.effect.IO
import cats.effect.implicits.concurrentParTraverseOps
import org.springframework.stereotype.Service
import scala.concurrent.duration.*


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

  /**
   * Simulates performing a series of cpu intensive tasks interspersed with waits, either
   * serially or in parallel.
   *
   * @param iterations          The number of iterations to perform.
   * @param iterationDurationMs The amount of "cpu work" to perform per iteration.
   * @param waitIntervalMs      The amount of time to "sleep" between iterations.
   *                            This can be used to simulate real world scenarios that involve
   *                            waiting for database connections etc.
   *                            Set to 0 to effectively disable.
   * @param parallelism         The number of tasks that may be run in parallel.  Setting to 1
   *                            effectively disables parallelism.
   * @return
   */
  def doCpuIntensiveThings(
    iterations: Int,
    iterationDurationMs: Long,
    waitIntervalMs: Long = 0,
    parallelism: Int
  ): IO[BenchmarkResult] = {
    val start = System.currentTimeMillis()
    (1 to iterations).toList.parTraverseN(parallelism) { _ =>
      for {
        result <- doSomethingCpuIntensive(waitIntervalMs)
        _ <- IO.sleep(waitIntervalMs.milliseconds)
      } yield result
      //doSomethingCpuIntensive(iterationDurationMs)
    }.flatMap { _ =>
      val totalDuration = System.currentTimeMillis() - start
      IO.pure(
        BenchmarkResult(
          totalDurationMs = totalDuration,
          individualDurationMs = iterationDurationMs,
          iterations = iterations
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
