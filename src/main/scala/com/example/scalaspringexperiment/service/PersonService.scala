package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import com.example.scalaspringexperiment.entity.Person
import doobie.DataSourceTransactor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersonService(
  //override val dao: Persistence
  override val ds: Resource[IO, DataSourceTransactor[IO]]
) extends Dao[Person] {

  override val logger = LoggerFactory.getLogger(classOf[FooService])
  override val tableInfo: TableInfo[Person] = TableInfo.build[Person]()
}
