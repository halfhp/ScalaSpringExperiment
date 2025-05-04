package com.example.scalaspringexperiment.controller

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.util.AsyncUtils
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.{ReactiveSecurityContextHolder, SecurityContext, SecurityContextHolder}
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
  import AsyncUtils.ioToMono

  private def fromIO [T](
    cb: () => IO[T],
  ): Mono[T] = cb()

  /**
   * Used by controller endpoints where authentication is not required.  If the user is authenticated, the authentication
   * will be made available in the Ctx object.  Also handles the conversion from IO[T] to Mono[T].
   * @param cb
   * @tparam T
   * @return
   */
  def maybeAuth[T](
    cb: Ctx => IO[T]
  ): Mono[T] = {
    ReactiveSecurityContextHolder.getContext
      .map(sc => Option(sc.getAuthentication))
      .defaultIfEmpty(None)
      .flatMap(auth => cb(Ctx(auth)))
  }

  /**
   * Used by controller endpoints where authentication is required.
   * Also handles the conversion from IO[T] to Mono[T].
   * @param cb
   * @tparam T
   * @return
   */
  def auth[T](
    cb: AuthCtx => IO[T]
  ): Mono[T] = {
    Option(AuthCtx(
      authentication = SecurityContextHolder.getContext.getAuthentication
    )) match {
      case None => ???
      case Some(auth) => cb(auth)
    }
  }
}
