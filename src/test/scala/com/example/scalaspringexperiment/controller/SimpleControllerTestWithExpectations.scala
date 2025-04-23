package com.example.scalaspringexperiment.controller

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.{AddressService, PersonService}
import com.example.scalaspringexperiment.test.SpringTestConfig
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{times, verify}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.`override`.mockito.MockitoSpyBean

import scala.compiletime.uninitialized

@SpringBootTest
@Import(Array(classOf[SpringTestConfig]))
class SimpleControllerTestWithExpectations {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @MockitoSpyBean
  var personService: PersonService = uninitialized

  @MockitoSpyBean
  var addressService: AddressService = uninitialized

  @Autowired
  implicit var runtime: IORuntime = uninitialized

  @Test
  def testGetDetailedPerson_invokesExpectedServiceMethods(): Unit = {
    val person = personService.insert(
      Person(name = "John Doe", age = 30)
    ).unsafeRunSync()

    simpleController.getDetailedPerson(person.id)
    verify(personService, times(1)).findById(person.id)
    verify(addressService, times(1)).findByPersonId(person.id)
  }
}
