package com.example.scalaspringexperiment.auth

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.auth.JwtAuthManager.{AuthError, InvalidCredentials, LoginSuccess, ROLE_USER, RegisterSuccess, UserExists, UserNotFound}
import com.example.scalaspringexperiment.entity.{Person, RegisteredUser}
import com.example.scalaspringexperiment.service.{PersonService, RegisteredUserService}
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.{ReactiveAuthenticationManager, UsernamePasswordAuthenticationToken}
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.{Authentication, GrantedAuthority}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import reactor.core.publisher.Mono

import java.security.SecureRandom
import java.time.Instant
import scala.jdk.CollectionConverters.*

object JwtAuthManager {
  val ROLE_USER = "ROLE_USER"
  val ROLE_ADMIN = "ROLE_ADMIN"
  sealed trait AuthError {
    val message: String
  }

  case class InvalidCredentials(
    message: String
  ) extends AuthError

  case class UserNotFound(
    message: String
  ) extends AuthError

  case class UserExists(
    message: String
  ) extends AuthError

  case class LoginSuccess(
    registeredUser: RegisteredUser,
    person: Person,
    jwtToken: String
  )

  case class RegisterSuccess(
    registeredUser: RegisteredUser,
    person: Person,
    jwtToken: String
  )
}

@Component
class JwtAuthManager(
  @Lazy personService: PersonService,
  @Lazy registeredUserService: RegisteredUserService,
  runtime: IORuntime,
) extends ReactiveAuthenticationManager {

  private val hasher = new BCryptPasswordEncoder(10, new SecureRandom())

  given theRuntime: IORuntime = runtime

  // TODO: this is very bad:
  private val secretKey: String = "secretKey"
  private val algo = JwtAlgorithm.HS256

  override def authenticate(authentication: Authentication): Mono[Authentication] = {
    println(s"[AUTH DEBUG] Called with credentials: ${authentication.getCredentials}")
    try {
      val token = authentication.getCredentials.toString
      val claim = JwtCirce.decode(token, secretKey, Seq(algo)).get
      val authorities = Seq(new SimpleGrantedAuthority(ROLE_USER))
      val auth = new UsernamePasswordAuthenticationToken(
        claim.subject.get,
        null,
        authorities.asJava
      )
      Mono.just(auth)
    } catch {
      case e: Exception => Mono.empty()
    }
  }


  def generateTokenForRegisteredUser(
    user: RegisteredUser,
    expiration: Instant
  ): IO[String] = IO {
    val claim = JwtClaim(
      subject = Some(user.id.toString),
      expiration = Some(expiration.getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    JwtCirce.encode(claim, secretKey, algo)
  }

  def validatePassword(
    password: String,
    passwordHash: String
  ): IO[Boolean] = {
    IO {
      hasher.matches(password, passwordHash)
    }.handleError { ex =>
      false
    }
  }

  def login(
    email: String,
    password: String
  ): IO[Either[AuthError, LoginSuccess]] = {
    (for {
      registeredUser <- EitherT(registeredUserService.findByEmail(email).map {
        case Some(user) => Right(user)
        case None => Left(UserNotFound("User not found"))
      })
      isValidPassword <- EitherT(validatePassword(password, registeredUser.passwordHash).map {
        case true => Right(true)
        case false => Left(InvalidCredentials("Invalid credentials"))
      })
      person <- EitherT(personService.findById(registeredUser.personId).map {
        case Some(p) => Right(p)
        case None => Left(UserNotFound("Person not found"))
      })
      jwtToken <- EitherT.liftF[IO, AuthError, String](generateTokenForRegisteredUser(registeredUser, Instant.now.plusSeconds(3600 * 30)))
    } yield LoginSuccess(
        registeredUser = registeredUser,
        person = person,
        jwtToken = jwtToken
      )).value
  }

  def register(
    name: String,
    age: Int,
    email: String,
    password: String
  ): IO[Either[AuthError, RegisterSuccess]] = {
    (for {
      existingUser <- EitherT(registeredUserService.findByEmail(email).map {
        case Some(_) => Left(UserExists("User already exists"))
        case None => Right(())
      })
      person <- EitherT.liftF(personService.insert(Person(
        name = name,
        age = age
      )))
      passwordHash <- EitherT.liftF(IO(hasher.encode(password)))
      registeredUser <- EitherT.liftF(registeredUserService.insert(RegisteredUser(
        email = email,
        passwordHash = passwordHash,
        personId = person.id,
        roles = List() // TODO
      )))
      jwtToken <- EitherT.liftF(generateTokenForRegisteredUser(registeredUser, Instant.now.plusSeconds(3600 * 30))) // 30 days
    } yield RegisterSuccess(
      registeredUser = registeredUser,
      person = person,
      jwtToken = jwtToken
    )).value
  }
}
