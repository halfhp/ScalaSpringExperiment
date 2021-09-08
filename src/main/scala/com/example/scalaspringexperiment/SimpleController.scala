package com.example.scalaspringexperiment
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RestController}

@RestController
@RequestMapping(Array("/scala"))
class SimpleController {

  @RequestMapping(method = Array(RequestMethod.GET))
  def test = "this is a test"

}
