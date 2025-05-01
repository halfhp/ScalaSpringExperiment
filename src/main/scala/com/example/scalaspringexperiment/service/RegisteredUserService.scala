package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import com.example.scalaspringexperiment.entity.RegisteredUser
import doobie.implicits.toSqlInterpolator
import doobie.{DataSourceTransactor, Fragment}
import doobie.util.{Read, Write}
import doobie.postgres.implicits.*
import doobie.implicits.*
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

@Service
class RegisteredUserService(
  override val ds: Resource[IO, DataSourceTransactor[IO]]
) extends Dao[RegisteredUser] {

  override val logger: Logger = LoggerFactory.getLogger(classOf[RegisteredUserService])
  override val tableInfo: TableInfo[RegisteredUser] = TableInfo.build[RegisteredUser]()
  override implicit val reader: Read[RegisteredUser] = Read.derived
  override implicit val writer: Write[RegisteredUser] = Write.derived

  private val theTableName = Fragment.const0(tableInfo.table.name)

  def findByEmail(email: String): IO[Option[RegisteredUser]] = ds.use { xa =>
    sql"""
      SELECT * FROM $theTableName
      WHERE email = $email
    """.query.option.transact(xa)
  }
}
