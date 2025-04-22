package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.SimpleService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
class SimpleControllerTest {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @Autowired
  var simpleService: SimpleService = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.resetDatabase()
  }

  @Test
  def testSomething(): Unit = {
    val result = simpleController.test()
    assert(result == "did something then did something else")
  }

  @Test
  def testGetFoo(): Unit = {
    val result = simpleController.getFoo()
    assertEquals("foo", result)
  }
}

