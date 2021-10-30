package com.example.scalaspringexperiment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RequestMapping, RequestMethod, RestController}
//import zio.Runtime
//import zio.{IO, Task, ZIO}
import cats.effect.{IO}
import cats.effect.unsafe.implicits.global
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.auto.*

import javax.annotation.security.PermitAll
import scala.language.implicitConversions

@PreAuthorize("true")
@RestController
@Autowired
class SimpleController(
  simpleService: SimpleService
) {

  implicit def ioToA[A](io: IO[A]): A = {
    io.unsafeRunSync()
    //Runtime.default.unsafeRun(zio)
  }

  //@PreAuthorize("true")
  @RequestMapping(path = Array("/scala"), method = Array(RequestMethod.GET))
  def test(): String = {
    for {
      something <- simpleService.doSomething()
      somethingElse <- simpleService.doSomethingElse()
    } yield (s"$something then $somethingElse")
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/foo"))
  def getFoo(): Json = {
    for {
      maybeFoo <- simpleService.getFoo()
      result <- IO.pure(maybeFoo.fold(
        left => { left.asJson },
        right => { right.asJson }
      ))
    } yield (result)
  }

  @PreAuthorize("permitAll()")
  @PostMapping(path = Array("/bar"))
  def postBar(
    @RequestBody requestBody: Json
  ): Json = {
    requestBody
  }
}
