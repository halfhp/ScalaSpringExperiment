package com.example.scalaspringexperiment.controller

import cats.data.EitherT
import com.example.scalaspringexperiment.service.{FooService, SimpleService}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.example.scalaspringexperiment.model.FooDomain
import doobie.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

import scala.language.implicitConversions

@PreAuthorize("true")
@RestController
@Autowired
class SimpleController(
  simpleService: SimpleService,
  fooService: FooService
) {

  implicit def ioToA[A](io: IO[A]): A = {
    io.unsafeRunSync()
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/scala"))
  def test(): String = {
    (for {
      something <- EitherT(simpleService.doSomething())
      somethingElse <- EitherT(simpleService.doSomethingElse())
    } yield s"$something then $somethingElse")
      .value
      .map(_.getOrElse("error"))
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/foo"))
  def getFoo(): Json = {
    for {
      fooList <- fooService.insert2(FooDomain(a = "new", b = 134))
      //result <- IO.pure(maybeFoo.fold(FooDomain(a = "oops", b = 999).asJson){ f => f.asJson })
      result <- IO.pure(fooList.asJson)
    } yield result
  }

  @PreAuthorize("permitAll()")
  @PostMapping(path = Array("/bar"))
  def postBar(
    @RequestBody requestBody: Json
  ): Json = {
    requestBody
  }
}
