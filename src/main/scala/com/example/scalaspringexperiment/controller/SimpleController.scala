package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.{AddressService, BenchmarkService, PersonService}
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
  addressService: AddressService,
  benchmarkService: BenchmarkService
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
  ): Mono[ResponseEntity[Json]] = {
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
