package com.example.scalaspringexperiment

import doobie.free.connection.ConnectionIO
import org.springframework.stereotype.Service
import cats.effect.IO
import cats.implicits.*
import doobie.*
import doobie.implicits.*
//import doobie.implicits.javatime.*
import doobie.postgres.implicits.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Service
@Autowired
class FooService(
  override val persistence: Persistence
) extends PersistenceLayer[FooDomain] {

  override val logger = LoggerFactory.getLogger(classOf[FooService])

  override val tableName = "foo"
  override val insertRows = "id, dateCreated, lastUpdated, a, b,"

  override def insertValues(model: FooDomain) = fr"${model.id}, ${model.dateCreated}, ${model.lastUpdated}, ${model.a}, ${model.b}"

  def insert2(model: FooDomain): IO[List[FooDomain]] = {
    val sql = "insert into foo (dateCreated, lastUpdated, a, b) values (?, ?, ?, ?)"
    Update[FooDomain](sql)
      //.updateMany(List(model))
      .updateManyWithGeneratedKeys[FooDomain]("id", "dateCreated", "lastUpdated", "a", "b")(List(model))
      //.update.withGeneratedKeys[FooDomain]("id")(model)
      .compile
      .toList
      .transact(persistence.xa)
  }

  case class FooServiceError(
    message: String
  )

  def getFoo(): IO[Either[FooServiceError, FooDomain]] = IO.pure {
    Either.right(FooDomain(
      a = "hi!",
      b = 2
    ))
  }
}
