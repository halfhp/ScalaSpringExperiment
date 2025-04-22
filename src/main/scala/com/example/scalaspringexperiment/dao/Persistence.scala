package com.example.scalaspringexperiment.dao

import cats.*
import cats.effect.*
import com.example.scalaspringexperiment.model.DomainModel
import doobie.*
import doobie.implicits.*
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.compiletime.uninitialized

@Service
class Persistence {

  @Autowired
  var ds: Resource[IO, DataSourceTransactor[IO]] = uninitialized

}

trait PersistenceLayer[T <: DomainModel] {
  val logger: Logger
  val persistence: Persistence
  val tableInfo: TableInfo[T]

  // TODO - figure out how to get this working again
  // implicit val logHandler: LogHandler = LogHandler(evt => logger.info(evt.toString))

  def insertValues(model: T): Fragment

  def insert(
    model: T
  )(
    implicit r: Read[T]
  ): IO[T] = persistence.ds.use { xa =>
    val theTableName = Fragment.const0(tableInfo.table.name)
    val theInsertCols = Fragment.const0(tableInfo.insertColumnNames.mkString(","))
    for {
      sql <- IO(sql"INSERT INTO $theTableName ($theInsertCols) VALUES (${insertValues(model)})")
      query <- IO(sql.update.withUniqueGeneratedKeys[T](tableInfo.columnNames *))
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
DELETE FROM ${tableInfo.table.name}
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
    (Fragment.const(s"select * from ${tableInfo.table.name} where id = ") ++ fr"$id LIMIT 1").query[T]
      .option
      .transact(xa)
  }
}
