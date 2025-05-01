package com.example.scalaspringexperiment.controller

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

import java.util.concurrent.CompletableFuture
import scala.util.chaining.*

case class Ctx(
  authentication: Option[Authentication]
)
case class AuthCtx(
  authentication: Authentication
)

@Component
class ControllerHelper(
  runtime: IORuntime
) {
  
  given rt: IORuntime = runtime
  
  private given ioToMono[A](): Conversion[IO[A], Mono[A]] with {
    def apply(io: IO[A]): Mono[A] = {
      Mono.fromFuture(new CompletableFuture[A]().tap { cf =>
        io.unsafeRunAsync {
          case Right(value) => cf.complete(value)
          case Left(error) => cf.completeExceptionally(error)
        }
      })
    }
  }

  private def fromIO [T](
    cb: () => IO[T],
  ): Mono[T] = cb()

  def maybeAuth[T](
    cb: Ctx => IO[T]
  ): Mono[T] = cb(Ctx(
    authentication = Option(SecurityContextHolder.getContext.getAuthentication
    )))

  def auth[T](
    cb: AuthCtx => T
  ): T = {
    Option(AuthCtx(
      authentication = SecurityContextHolder.getContext.getAuthentication
    )) match {
      case None => ???
      case Some(auth) => cb(auth)
    }
  }
}
