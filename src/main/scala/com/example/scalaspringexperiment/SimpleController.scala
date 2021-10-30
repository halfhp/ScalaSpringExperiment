package com.example.scalaspringexperiment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestMethod, RestController}
import zio.Runtime
import zio.{IO, Task, ZIO}
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

  implicit def zToA[E, A](zio: ZIO[Any, E, A]): A = {
    Runtime.default.unsafeRun(zio)
  }

  //@PreAuthorize("true")
  @RequestMapping(path = Array("/scala"), method = Array(RequestMethod.GET))
  def test(): String = {
    for {
      something <- simpleService.doSomething()
      somethingElse <- simpleService.doSomethingElse()
    } yield (s"$something then $somethingElse")
  }

  @PermitAll
  @PreAuthorize("permitAll()")
  @RequestMapping(path = Array("/foo"), method = Array(RequestMethod.GET))
  def getFoo(): String = {
    for {
      foo <- simpleService.getFoo()
    } yield (foo.asJson.noSpaces)
  }

}
