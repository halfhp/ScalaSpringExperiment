package com.example.scalaspringexperiment.controller

import cats.effect.IO
import com.example.scalaspringexperiment.auth.JwtAuthManager
import com.example.scalaspringexperiment.util.JsonUtils.stripFields
import io.circe.Json
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import com.example.scalaspringexperiment.util.MyJsonCodecs.*
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RestController}
import reactor.core.publisher.Mono

@RestController
class AuthController(
  jwtAuthManager: JwtAuthManager,
  helper: ControllerHelper
) {

  case class RegisterRequest(
    name: String,
    age: Int,
    email: String,
    password: String
  )

  @PreAuthorize("permitAll()")
  @PostMapping(path = Array("/register"))
  def register(
    @RequestBody requestBody: Json
  ): Mono[ResponseEntity[Json]] = helper.maybeAuth { _ =>
    for {
      registerRequest <- IO(requestBody.as[RegisterRequest].getOrElse(???))
      result <- jwtAuthManager.register(
        name = registerRequest.name,
        age = registerRequest.age,
        email = registerRequest.email,
        password = registerRequest.password
      )
    } yield result match {
      case Left(error) => error match {
        case _: JwtAuthManager.UserExists =>
          ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Json.obj("error" -> "Email address already registered".asJson))
        case _ => ???
      }
      case Right(r) => ResponseEntity.ok(r.asJson.stripFields("passwordHash"))
    }
  }

  case class LoginRequest(
    email: String,
    password: String
  )

  @PreAuthorize("permitAll()")
  @PostMapping(path = Array("/login"))
  def login(
    @RequestBody requestBody: Json
  ): Mono[ResponseEntity[Json]] = helper.maybeAuth { _ =>
    for {
      credentials <- IO(requestBody.as[LoginRequest].getOrElse(???))
      auth <- jwtAuthManager.login(
        email = credentials.email,
        password = credentials.password
      )

      response <- auth match {
        case Left(error) => error match {
          case _: JwtAuthManager.InvalidCredentials =>
            IO(ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(Json.obj("error" -> "Invalid credentials".asJson))
            )
          case _: JwtAuthManager.UserNotFound =>
            IO(ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(Json.obj("error" -> "User not found".asJson))
            )
          case _ => ???
        }
        case Right(auth) => IO(ResponseEntity.ok(auth.asJson.stripFields("passwordHash")))
      }
    } yield response
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("authtest/optional"))
  def maybeAuthTest(): Mono[ResponseEntity[Json]] = helper.maybeAuth { ctx =>
    IO(ResponseEntity.ok(Json.obj(
      "isAuthenticated" -> ctx.authentication.isDefined.asJson,
    )))
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping(path = Array("authtest/required"))
  def requiredAuthTest(): Mono[ResponseEntity[Json]] = helper.auth { ctx =>
    IO(ResponseEntity.ok(Json.obj()))
  }

  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping(path = Array("authtest/user"))
  def userAuthTest(): Mono[ResponseEntity[Json]] = helper.auth { ctx =>
    IO(ResponseEntity.ok(Json.obj()))
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping(path = Array("authtest/admin"))
  def adminAuthTest(): Mono[ResponseEntity[Json]] = helper.auth { ctx =>
    IO(ResponseEntity.ok(Json.obj()))
  }
}
