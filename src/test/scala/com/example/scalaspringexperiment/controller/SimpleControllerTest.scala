package com.example.scalaspringexperiment.controller

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.PersonService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

import scala.compiletime.uninitialized

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(Array(classOf[SpringTestConfig]))
class SimpleControllerTest {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @Autowired
  var personService: PersonService = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @Autowired
  implicit var runtime: IORuntime = uninitialized

  @Autowired
  var webTestClient: WebTestClient = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.truncateTables()
  }

  @Test
  def getDetailedPersonById_returnsPerson(): Unit = {
    val person = personService.insert(
      Person(name = "John Doe", age = 30)
    ).unsafeRunSync()

    webTestClient.get()
      .uri(s"/person/${person.id}/detailed")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.person.name").isEqualTo("John Doe")
      .jsonPath("$.person.age").isEqualTo(30)
  }

  @Test
  def getDetailedPersonById_returns404_ifPersonNotFound(): Unit = {
    webTestClient.get()
      .uri("/person/999/detailed")
      .exchange()
      .expectStatus().isNotFound
  }
}
