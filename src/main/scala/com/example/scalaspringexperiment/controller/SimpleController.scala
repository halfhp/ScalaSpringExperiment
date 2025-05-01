package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.{AddressService, BenchmarkService, PersonService}
import cats.effect.IO
import com.example.scalaspringexperiment.controller
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
import scala.language.implicitConversions

/**
 * A simple async REST controller
 * For auth related endpoints see AuthController.
 */
@RestController
class SimpleController(
  personService: PersonService,
  addressService: AddressService,
  helper: ControllerHelper
) {

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
  ): Mono[Json] = helper.maybeAuth { _ =>
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
  ): Mono[ResponseEntity[Json]] = helper.maybeAuth { _ =>
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
