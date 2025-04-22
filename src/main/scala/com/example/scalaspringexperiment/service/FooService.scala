package com.example.scalaspringexperiment.service

import cats.implicits.*
import com.example.scalaspringexperiment.model.FooDomain
import com.example.scalaspringexperiment.dao.{Persistence, PersistenceLayer, TableInfo}
import doobie.*
import doobie.implicits.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Service
@Autowired
class FooService(
  override val persistence: Persistence
) extends PersistenceLayer[FooDomain] {

  override val logger = LoggerFactory.getLogger(classOf[FooService])
  override val tableInfo: TableInfo[FooDomain] = TableInfo.build[FooDomain]()

  override def insertValues(model: FooDomain) = fr"${model.dateCreated},${model.lastUpdated},${model.a},${model.b}"

  case class FooServiceError(
    message: String
  )
}
