package com.example.scalaspringexperiment.test

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import doobie.DataSourceTransactor
import doobie.implicits.toSqlInterpolator
import org.springframework.stereotype.Service
import doobie.syntax.all.*

@Service
class TestUtils(
  val ds: Resource[IO, DataSourceTransactor[IO]],
  implicit val runtime: IORuntime
) {

  def truncateTables(): Unit = {
    ds.use { xa =>
      for {
        tables <- sql"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
          .query[String].to[List].transact(xa)
        results <- IO {
          // leave flyway_schema_history in place:
          tables.filter(_ != "flyway_schema_history").map { table =>
            sql"TRUNCATE TABLE $table CASCADE".update.run.transact(xa)
          }
        }
      } yield results
    }.unsafeRunSync()
  }
}


