package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.controller.SimpleController
import com.example.scalaspringexperiment.service.SimpleService
import com.example.scalaspringexperiment.test.MyTestConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{times, verify}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.`override`.mockito.MockitoSpyBean

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[MyTestConfig]))
class SimpleControllerTestWithExpectations {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @MockitoSpyBean
  var simpleServiceMock: SimpleService = uninitialized

  @Test
  def testOne(): Unit = {
    simpleController.test()
    verify(simpleServiceMock, times(1)).doSomething()
    verify(simpleServiceMock, times(1)).doSomethingElse()
  }
}