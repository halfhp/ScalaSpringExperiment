package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.SimpleService
import com.example.scalaspringexperiment.test.MyTestConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[MyTestConfig]))
class SimpleControllerTest {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @Autowired
  var simpleService: SimpleService = uninitialized

  @Test
  def testSomething(): Unit = {
    val result = simpleController.test()
    assert(result == "did something then did something else")
  }
}

