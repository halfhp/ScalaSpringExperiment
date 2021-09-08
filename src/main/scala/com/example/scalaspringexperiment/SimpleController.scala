package com.example.scalaspringexperiment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RestController}
import zio.Runtime

@RestController
@RequestMapping(path = Array("/scala"))
@Autowired
class SimpleController(
  simpleService: SimpleService
) {

  @RequestMapping(method = Array(RequestMethod.GET))
  def test = {
    Runtime.default.unsafeRun(simpleService.doSomething())
  }
}
