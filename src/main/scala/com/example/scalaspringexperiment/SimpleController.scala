package com.example.scalaspringexperiment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RestController}
import zio.Runtime
import zio.{Task, ZIO, IO}

@RestController
@Autowired
class SimpleController(
  simpleService: SimpleService
) {

  implicit def zToA[E, A](zio: ZIO[Any, E, A]): A = {
    Runtime.default.unsafeRun(zio)
  }

  @RequestMapping(path = Array("/scala"), method = Array(RequestMethod.GET))
  def test(): String = {
    for {
      something <- simpleService.doSomething()
      somethingElse <- simpleService.doSomethingElse()
    } yield (s"$something then $somethingElse")
  }
}
