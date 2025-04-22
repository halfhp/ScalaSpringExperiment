package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import com.example.scalaspringexperiment.entity.Person
import doobie.DataSourceTransactor
import doobie.util.{Read, Write}
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersonService(
  override val ds: Resource[IO, DataSourceTransactor[IO]]
) extends Dao[Person] {

  override val logger = LoggerFactory.getLogger(classOf[PersonService])
  override val tableInfo: TableInfo[Person] = TableInfo.build[Person]()
  override implicit val reader: Read[Person] = Read.derived
  override implicit val writer: Write[Person] = Write.derived
}
