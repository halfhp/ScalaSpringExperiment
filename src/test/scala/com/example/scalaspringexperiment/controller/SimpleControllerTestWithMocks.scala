package com.example.scalaspringexperiment.controller

import cats.effect.IO
import com.example.scalaspringexperiment.controller.SimpleController
import com.example.scalaspringexperiment.service.SimpleService
import com.example.scalaspringexperiment.test.SpringTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.when
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.`override`.mockito.MockitoBean

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
class SimpleControllerTestWithMocks {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @MockitoBean
  var simpleServiceMock: SimpleService = uninitialized

  @Test
  def testOne(): Unit = {
    when(simpleServiceMock.doSomething()).thenReturn(IO.pure(Right("First")))
    when(simpleServiceMock.doSomethingElse()).thenReturn(IO.pure(Right("Second")))

    assertEquals("First then Second", simpleController.test())
  }
}

