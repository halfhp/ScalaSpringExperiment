package com.example.scalaspringexperiment.controller

import cats.data.OptionT
import com.example.scalaspringexperiment.service.{AddressService, PersonService}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.example.scalaspringexperiment.entity.Person
import doobie.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import com.example.scalaspringexperiment.util.MyJsonCodecs.timestampCodec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

import scala.language.implicitConversions

@PreAuthorize("true")
@RestController
@Autowired
class SimpleController(
  personService: PersonService,
  addressService: AddressService
) {

  implicit def ioToA[A](io: IO[A]): A = {
    io.unsafeRunSync()
  }

  @PreAuthorize("permitAll()")
  @GetMapping(path = Array("/person/{id}/detailed"))
  def getDetailedPerson(
    @PathVariable id: Long,
  ): Json = {
    (for {
      person <- OptionT(personService.findById(id))
      addresses <- OptionT.liftF(addressService.findByPersonId(id))
    } yield Json.obj(
      "person" -> person.asJson,
      "addresses" -> addresses.asJson
    )).value.getOrElse(Json.obj())
  }
}
