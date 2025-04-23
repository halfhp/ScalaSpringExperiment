package com.example.scalaspringexperiment.controller

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.PersonService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import io.circe.generic.auto.*
import com.example.scalaspringexperiment.util.MyJsonCodecs.timestampCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

import scala.compiletime.uninitialized
import scala.util.chaining.*

@SpringBootTest()
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

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.resetDatabase()
  }

  @Test
  def testGetDetailedPerson(): Unit = {
    val person = personService.insert(
      Person(name = "John Doe", age = 30)
    ).unsafeRunSync()

    val result = simpleController.getDetailedPerson(person.id)

    result.hcursor.get[Person]("person").getOrElse(???).tap { p =>
      assertEquals(person.id, p.id)
      assertEquals(person.name, p.name)
    }
  }
}

