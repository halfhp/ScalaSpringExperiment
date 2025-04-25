package com.example.scalaspringexperiment.service

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Address
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import com.example.scalaspringexperiment.util.PointUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

import scala.compiletime.uninitialized
import scala.util.chaining.*

@SpringBootTest()
@Import(Array(classOf[SpringTestConfig]))
class AddressServiceTest {

  @Autowired
  var addressService: AddressService = uninitialized

  @Autowired
  var testUtils: TestUtils = uninitialized

  @Autowired
  implicit var runtime: IORuntime = uninitialized

  @BeforeEach
  def beforeEach(): Unit = {
    testUtils.truncateTables()
  }

  @Test
  def findWithinDistance_returnsOnlyAddressesWithinDistance(): Unit = {
    val personInAustin = testUtils.newRandomPerson(persist = true).tap { p =>
      addressService.insert(
        Address(
          personId = p.id,
          street = Some("123 Main St"),
          city = Some("Austin"),
          state = Some("TX"),
          coordinates = Some(PointUtils.pointFromLatLon(30.2673, 97.7432))
        )
      ).unsafeRunSync()
    }

    val personInSanAntonio = testUtils.newRandomPerson(persist = true).tap { p =>
      addressService.insert(
        Address(
          personId = p.id,
          street = Some("100 San Antonio Blvd"),
          city = Some("San Antonio"),
          state = Some("TX"),
          coordinates = Some(PointUtils.pointFromLatLon(29.4252, 98.4946))
        )
      ).unsafeRunSync()
    }

    val addressesNearAustin = addressService.findWithinDistance(
      lat = 30.2672,
      lon = 97.7431,
      distanceInMeters = 500
    ).unsafeRunSync()

    assertEquals(1, addressesNearAustin.length)
    assertEquals("Austin", addressesNearAustin.head.city.get)
  }
}
