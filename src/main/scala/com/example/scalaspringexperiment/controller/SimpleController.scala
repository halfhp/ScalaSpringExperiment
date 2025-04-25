package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.{AddressService, PersonService}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.example.scalaspringexperiment.entity.Person
import doobie.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import com.example.scalaspringexperiment.util.MyJsonCodecs.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

import java.util.concurrent.CompletableFuture
import scala.language.implicitConversions
import scala.util.chaining.*

/**
 * A simple async REST controller
 */
@RestController
class SimpleController(
  personService: PersonService,
  addressService: AddressService
) {

  private implicit def ioToMono[A](io: IO[A]): Mono[A] = {
    Mono.fromFuture(new CompletableFuture[A]().tap { cf =>
      io.unsafeRunAsync {
        case Right(value) => cf.complete(value)
        case Left(error) => cf.completeExceptionally(error)
      }
    })
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/"))
  def index(): Mono[ResponseEntity[Json]] = {
    Mono.just(ResponseEntity.ok(Json.obj(
      "message" -> Json.fromString("Hello, world!"),
    )))
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/person/{id}"))
  def getPersonById(
    @PathVariable id: Long,
  ): Mono[Json] = {
    for {
      person <- personService.findById(id)
    } yield Json.obj(
      "person" -> person.asJson,
    )
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/person/{id}/detailed"))
  def getDetailedPersonById(
    @PathVariable id: Long,
  ): Mono[ResponseEntity[Json]] = {
    for {
      person <- personService.findById(id)
      addresses <- addressService.findByPersonId(id)
    } yield person match {
      case Some(person) => ResponseEntity.ok(Json.obj(
        "person" -> person.asJson,
        "addresses" -> addresses.asJson
      ))
      case None => ResponseEntity.notFound().build()
    }
  }
}
