package com.example.scalaspringexperiment.controller

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.{AddressService, PersonService}
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import io.circe.generic.auto.*
import com.example.scalaspringexperiment.util.MyJsonCodecs.timestampCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.`override`.mockito.MockitoBean

import scala.compiletime.uninitialized
import scala.util.chaining.*

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
class SimpleControllerTestWithMocks {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @MockitoBean
  var personService: PersonService = uninitialized

  @MockitoBean
  var addressService: AddressService = uninitialized

  @Autowired
  implicit var runtime: IORuntime = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.truncateTables()
  }

  @Test
  def getDetailedPerson_rendersDetailedPersonJson(): Unit = {
    val person = Person(id = 123, name = "John Doe", age = 30)
    when(personService.findById(anyLong())).thenReturn(IO.pure(Some(person)))
    when(addressService.findByPersonId(anyLong())).thenReturn(IO.pure(List()))

    simpleController.getDetailedPerson(person.id).tap { json =>
      val decodedPerson = json.hcursor.get[Person]("person").getOrElse(???)
      assertEquals(person.id, decodedPerson.id)
    }
  }
}

