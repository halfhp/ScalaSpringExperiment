package com.example.scalaspringexperiment.test

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import com.example.scalaspringexperiment.entity.Person
import com.example.scalaspringexperiment.service.PersonService
import com.github.javafaker.Faker
import doobie.{DataSourceTransactor, Fragment}
import doobie.implicits.toSqlInterpolator
import org.springframework.stereotype.Service
import doobie.syntax.all.*
import org.springframework.beans.factory.annotation.Autowired

import scala.compiletime.uninitialized

@Service
class TestUtils(
  val ds: Resource[IO, DataSourceTransactor[IO]],
  implicit val runtime: IORuntime
) {

  @Autowired
  var personService: PersonService = uninitialized

  val faker = new Faker()

  private val ignoredTables = Seq(
    "flyway_schema_history",
    "geometry_columns",
    "geography_columns",
    "spatial_ref_sys",
  )

  // TODO: this isn't ideal; as new extensions etc. get installed, this approach can mangle them.
  // TODO: a more ideal approach would be sealing the entity trait and enumerating the table annotations
  // TODO: from there, but sealing means storing all entities in a single file, which isnt great either.
  def truncateTables(): Unit = {
    println("TRUNCATING ALL TABLES")
    ds.use { xa =>
      for {
        tables <- sql"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
          .query[String].to[List].transact(xa)
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
}


