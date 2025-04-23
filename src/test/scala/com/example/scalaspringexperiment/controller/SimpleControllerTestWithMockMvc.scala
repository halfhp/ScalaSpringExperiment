package com.example.scalaspringexperiment.controller

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.PersonService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
@AutoConfigureMockMvc
class SimpleControllerTestWithMockMvc {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @Autowired
  var personService: PersonService = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @Autowired
  implicit var runtime: IORuntime = uninitialized

  @Autowired
  var mockMvc: MockMvc = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.truncateTables()
  }

  @Test
  def testGetDetailedPerson(): Unit = {
    val person = personService.insert(
      Person(name = "John Doe", age = 30)
    ).unsafeRunSync()

    mockMvc.perform(
      MockMvcRequestBuilders.get(s"/person/${person.id}/detailed")
    ).andExpect(MockMvcResultMatchers.status().isOk)
     .andExpect(MockMvcResultMatchers.jsonPath("$.person.name").value("John Doe"))
     .andExpect(MockMvcResultMatchers.jsonPath("$.person.age").value(30))
  }
}

