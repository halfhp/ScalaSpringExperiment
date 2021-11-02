package com.example.scalaspringexperiment

import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.concurrent.ExecutionContext
import javax.sql.DataSource

@Service
@Autowired
class Persistence(
  dataSource: DataSource
) {
  val xa = Transactor.fromDataSource[IO](dataSource, scala.concurrent.ExecutionContext.global)
}

trait PersistenceLayer[T <: DomainModel] {
  val persistence: Persistence
  val tableName: String = "foo"

  private val selectAllFragment = Fragment.const(s"SELECT * FROM $tableName")

  def insert(model: T): IO[Option[T]]

  def update(model: T): IO[Option[T]]

  def delete(model: T)(implicit tt: Read[T]): IO[Option[T]] = {
    val q =
      sql"""
DELETE FROM $tableName
WHERE id = ${model.id}
"""
    for {
      id <- q.update.run.transact(persistence.xa)
      f <- findById(id)
    } yield f
  }

  def findById(id: Long)(implicit tt: Read[T]): IO[Option[T]] = {
    (Fragment.const(s"select * from $tableName where id = ") ++ fr"$id LIMIT 1").query[T]
      .option
      .transact(persistence.xa)
  }
}
