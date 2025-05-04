package com.example.scalaspringexperiment.test

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.auth.JwtAuthManager
import com.example.scalaspringexperiment.dao.Dao
import com.example.scalaspringexperiment.entity.{Person, RegisteredUser}
import com.example.scalaspringexperiment.service.{PersonService, RegisteredUserService}
import com.github.javafaker.Faker
import doobie.{DataSourceTransactor, Fragment}
import doobie.implicits.toSqlInterpolator
import org.springframework.stereotype.Service
import doobie.syntax.all.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.EntityExchangeResult

import java.util.function.Consumer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.annotation.StaticAnnotation

case class Table(name: String) extends StaticAnnotation

object TestUtils {
  val responsePrinter: Consumer[EntityExchangeResult[Array[Byte]]] = r => {
    Option(r.getResponseBody).map { body =>
      import io.circe.parser.*
      parse(new String(body, java.nio.charset.StandardCharsets.UTF_8)) match {
        case Left(error) =>
          println(body)
        case Right(json) =>
          val prettyJson = json.spaces2
          println(prettyJson)
      }
    }
  }
}

@Service
class TestUtils(
  val ds: Resource[IO, DataSourceTransactor[IO]],
  implicit val runtime: IORuntime
) {

  @Autowired
  var personService: PersonService = uninitialized

  @Autowired
  var registeredUserService: RegisteredUserService = uninitialized

  @Autowired
  var jwtAuthManager: JwtAuthManager = uninitialized

  @Autowired
  var applicationContext: ApplicationContext = uninitialized

  val faker = new Faker()

  private val ignoredTables = Seq(
    "flyway_schema_history",
    "geometry_columns",
    "geography_columns",
    "spatial_ref_sys",
  )

  def getAllDaos(): Seq[Dao[?]] = {
    applicationContext
      .getBeansOfType(classOf[Dao[?]])
      .values()
      .asScala
      .toSeq
  }

  def truncateTables(): Unit = {
    println("TRUNCATING ALL TABLES")
    ds.use { xa =>
      for {
        tables <- IO(getAllDaos().map(_.tableInfo.table.name))
        results <- IO {
          tables.filter(!ignoredTables.contains(_)).map { table =>
            val theTableName = Fragment.const0(table)
            sql"TRUNCATE TABLE $theTableName CASCADE".update.run.transact(xa).unsafeRunSync()
          }
        }
      } yield results
    }.unsafeRunSync()
  }

  def newRandomPerson(
    persist: Boolean = false,
    changes: Person => Person = {p => p}
  ): Person = {
    (for {
      unsavedPerson <- IO(Person(
        name = faker.name().fullName(),
        age = faker.number().numberBetween(18, 79)
      ))
      changedPerson = changes(unsavedPerson)
      finalizedPerson <- persist match {
        case true => personService.insert(changedPerson)
        case false => IO.pure(changedPerson)
      }
    } yield finalizedPerson).unsafeRunSync()
  }

  def newRegisteredUser(
    name: String = faker.name().fullName(),
    age: Int = faker.number().numberBetween(18, 79),
    email: String = faker.internet().emailAddress(),
    password: String = faker.internet().password(),
  ): (RegisteredUser, Person, String) = {
    (for {
      r <- jwtAuthManager.register(name, age, email, password).map(_.getOrElse(???))
    } yield (
      r.registeredUser,
      r.person,
      r.jwtToken
    )).unsafeRunSync()
  }
}


