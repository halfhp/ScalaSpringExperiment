package com.example.scalaspringexperiment.service

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

import scala.compiletime.uninitialized
import scala.util.chaining.*

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
class PersonServiceTest {
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
  def insert_insertsPerson(): Unit = {
    personService.insert(Person(
      id = 0,
      name = "John Doe",
      age = 30
    )).unsafeRunSync().tap { person =>
      assert(person.id > 0)
      assertEquals("John Doe", person.name)
      assertEquals(30, person.age)
    }
  }

  @Test
  def findById_returnsPerson(): Unit = {
    val insertedPerson = personService.insert(Person(
      id = 0,
      name = "John Doe",
      age = 30
    )).unsafeRunSync()

    personService.findById(insertedPerson.id).unsafeRunSync().get.tap { person =>
      assertEquals(insertedPerson.id, person.id)
      assertEquals("John Doe", person.name)
      assertEquals(30, person.age)
    }
  }
}
