package com.example.scalaspringexperiment.dao

import cats.*
import cats.effect.*
import com.example.scalaspringexperiment.dao.Persistence
import com.example.scalaspringexperiment.model.DomainModel
import doobie.*
import doobie.implicits.*
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.compiletime.uninitialized

@Service
@Autowired
class Persistence {

  @Autowired
  var ds: Resource[IO, DataSourceTransactor[IO]] = uninitialized

}

trait PersistenceLayer[T <: DomainModel] {
  val logger: Logger
  val persistence: Persistence
  val tableName: String
  val insertRows: String

  // TODO - figure out how to get this working again
  // implicit val logHandler: LogHandler = LogHandler(evt => logger.info(evt.toString))

  def insertValues(model: T): Fragment

  private val selectAllFragment = Fragment.const(s"SELECT * FROM $tableName")

  def insert(
    model: T
  )(
    implicit r: Read[T]
  ): IO[T] = persistence.ds.use { xa =>
    for {
      fr <- IO.pure(Fragment.const(s"INSERT into $tableName ($insertRows)") ++ fr" VALUES (" ++ insertValues(model) ++ fr")")
      query <- IO(fr.update.withUniqueGeneratedKeys[T](Seq("") *))
      result <- query.transact(xa)
    } yield result
  }

  // TODO
  def update(
    model: T
  ): IO[Option[T]] = ???

  def delete(
    model: T
  )(
    implicit r: Read[T]
  ): IO[Option[T]] = persistence.ds.use { xa =>
    val q =
      sql"""
DELETE FROM $tableName
WHERE id = ${model.id}
"""
    for {
      id <- q.update.run.transact(xa)
      f <- findById(id)
    } yield f
  }

  def findById(
    id: Long
  )(
    implicit r: Read[T]
  ): IO[Option[T]] = persistence.ds.use { xa =>
    (Fragment.const(s"select * from $tableName where id = ") ++ fr"$id LIMIT 1").query[T]
      .option
      .transact(xa)
  }
}
