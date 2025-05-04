package com.example.scalaspringexperiment.controller

import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.service.PersonService
import com.example.scalaspringexperiment.test.{SpringTestConfig, TestUtils}
import org.junit.jupiter.api.{BeforeEach, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

import scala.compiletime.uninitialized

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(Array(classOf[SpringTestConfig]))
class AuthControllerTest {

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
  def register_registersUser(): Unit = {
    webTestClient.post()
      .uri("/register")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        s"""
           |{
           |  "name": "Test User",
           |  "age": 30,
           |  "email": "test@test.com",
           |  "password": "testpassword"
           |}
           |""".stripMargin)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
      .jsonPath("$.registeredUser.email").isEqualTo("test@test.com")
      .jsonPath("$.registeredUser.passwordHash").doesNotExist()
      .jsonPath("$.jwtToken").exists()
  }

  @Test
  def login_succeeds_forValidPassword(): Unit = {
    val password = "thePassword"
    val (registeredUser, _, _) = testUtils.newRegisteredUser(password = password)
    webTestClient.post()
      .uri("/login")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        s"""
           |{
           |  "email": "${registeredUser.email}",
           |  "password": "$password"
           |}
           |""".stripMargin)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
      .jsonPath("$.registeredUser.email").isEqualTo(registeredUser.email)
      .jsonPath("$.registeredUser.passwordHash").doesNotExist()
      .jsonPath("$.jwtToken").exists()
  }

  @Test
  def login_fails_forInvalidPassword(): Unit = {
    val (registeredUser, _, _) = testUtils.newRegisteredUser()
    webTestClient.post()
      .uri("/login")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        s"""
           |{
           |  "email": "${registeredUser.email}",
           |  "password": "theWrongPassword"
           |}
           |""".stripMargin)
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
  }

  @Test
  def login_fails_forDuplicatedEmail(): Unit = {
    val theEmailAddress = "test@test.com"
    val (registeredUser, _, _) = testUtils.newRegisteredUser(email = theEmailAddress)
    webTestClient.post()
      .uri("/register")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        s"""
           |{
           |  "name": "Test User",
           |  "age": 30,
           |  "email": "$theEmailAddress",
           |  "password": "testpassword"
           |}
           |""".stripMargin)
      .exchange()
      .expectStatus().isEqualTo(409)
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
  }

  @Test
  def maybeAuth_showsAuthenticated_forAuthenticatedRequest(): Unit = {
    val (_, _, token) = testUtils.newRegisteredUser()
    webTestClient.get()
      .uri("/authtest/optional")
      .header("Authorization", s"Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
      .jsonPath("$.isAuthenticated").isEqualTo(true)
  }

  @Test
  def maybeAuth_showsUnauthenticated_forUnauthenticatedRequest(): Unit = {
    webTestClient.get()
      .uri("/authtest/optional")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
      .jsonPath("$.isAuthenticated").isEqualTo(false)
  }

  @Test
  def requiredAuth_returnsUnauthorized_forUnauthenticatedRequest(): Unit = {
    webTestClient.get()
      .uri("/authtest/required")
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
  }

  @Test
  def userAuthTest_returnsOk_forAuthenticatedUserRequest(): Unit = {
    val (_, _, token) = testUtils.newRegisteredUser()
    webTestClient.get()
      .uri("/authtest/user")
      .header("Authorization", s"Bearer $token")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
  }

  @Test
  def adminAuthTest_returnsForbidden_forAuthenticatedNonAdminRequest(): Unit = {
    val (_, _, token) = testUtils.newRegisteredUser()
    webTestClient.get()
      .uri("/authtest/admin")
      .header("Authorization", s"Bearer $token")
      .exchange()
      .expectStatus().isEqualTo(403)
      .expectBody()
      .consumeWith(TestUtils.responsePrinter)
  }
}
