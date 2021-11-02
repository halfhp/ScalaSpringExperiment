package com.example.scalaspringexperiment

import doobie.free.connection.ConnectionIO
import org.springframework.stereotype.Service
import cats.effect.IO
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatime._
import org.springframework.beans.factory.annotation.Autowired

@Service
@Autowired
class FooService(
  override val persistence: Persistence
) extends PersistenceLayer[FooDomain] {

  override val tableName = "foo"

  case class FooServiceError(
    message: String
  )

  def getFoo(): IO[Either[FooServiceError, FooDomain]] = IO.pure {
    Either.right(FooDomain(
      a = "hi!",
      b = 2
    ))
  }

  def insert(model: FooDomain): cats.effect.IO[Option[FooDomain]] = {
    val q =
      sql"""
INSERT INTO foo (a, b)
VALUES (${model.a}, ${model.b})
"""
    for {
      id <- q.update.run.transact(persistence.xa)
      f <- findById(id)
    } yield f
  }

  def update(model: FooDomain): cats.effect.IO[Option[FooDomain]] = ???
}
