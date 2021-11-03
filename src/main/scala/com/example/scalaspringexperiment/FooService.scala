package com.example.scalaspringexperiment

import doobie.free.connection.ConnectionIO
import org.springframework.stereotype.Service
import cats.effect.IO
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatime.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Service
@Autowired
class FooService(
  override val persistence: Persistence
) extends PersistenceLayer[FooDomain] {

  override val logger = LoggerFactory.getLogger(classOf[FooService])

  override val tableName = "foo"
  override val insertRows = "a, b"

  override def insertValues(model: FooDomain) = fr"${model.a}, ${model.b}"

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
