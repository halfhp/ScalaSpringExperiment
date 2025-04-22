package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import cats.implicits.*
import com.example.scalaspringexperiment.entity.FooDomain
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import doobie.DataSourceTransactor
import doobie.util.{Read, Write}
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Service
class FooService(
  //override val dao: Persistence
  override val ds: Resource[IO, DataSourceTransactor[IO]]
) extends Dao[FooDomain] {

  override val logger = LoggerFactory.getLogger(classOf[FooService])
  override val tableInfo: TableInfo[FooDomain] = TableInfo.build[FooDomain]()
  override implicit val reader: Read[FooDomain] = Read.derived
  override implicit val writer: Write[FooDomain] = Write.derived

  case class FooServiceError(
    message: String
  )
}
