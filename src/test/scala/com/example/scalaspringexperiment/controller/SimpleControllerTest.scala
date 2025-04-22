package com.example.scalaspringexperiment.controller

import com.example.scalaspringexperiment.service.PersonService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import io.circe.parser.*
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
  var personService: PersonService = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.resetDatabase()
  }

  @Test
  def testGeneratePerson(): Unit = {
    val result = simpleController.generatePerson()
    assertEquals("John Doe", result)
  }
//
//  @Test
//  def testGetFoo(): Unit = {
//    val result = simpleController.getFoo()
//    assertEquals(parse(
//      """
//        |{
//        |  "id" : 38,
//        |  "dateCreated" : 0,
//        |  "lastUpdated" : 0,
//        |  "a" : "new",
//        |  "b" : 134
//        |}
//        |""".stripMargin), result)
//  }
}

