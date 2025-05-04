package com.example.scalaspringexperiment.util

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import reactor.core.publisher.Mono

import java.util.concurrent.CompletableFuture
import scala.util.chaining.*

object AsyncUtils {

  /**
   * Convert an IO[A] to a Mono[A].  This implemntation should be efficient in the sense that while Mono is waiting for
   * the IO to resolve, it will not block a thread.  The IO will be run on the provided IORuntime, and the Mono threadpool will be free
   * to process other Mono tasks while it waits.
   */
  given ioToMono[A]()(
    using runtime: IORuntime
  ): Conversion[IO[A], Mono[A]] with {
    def apply(io: IO[A]): Mono[A] = {
      Mono.fromFuture(new CompletableFuture[A]().tap { cf =>
        io.unsafeRunAsync {
          case Right(value) => cf.complete(value)
          case Left(error) => cf.completeExceptionally(error)
        }
      })
    }
  }
}
