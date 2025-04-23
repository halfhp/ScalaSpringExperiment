package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import com.example.scalaspringexperiment.entity.Address
import doobie.{DataSourceTransactor, Fragment}
import doobie.implicits.toSqlInterpolator
import doobie.util.{Read, Write}
import doobie.implicits.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AddressService(
  override val ds: Resource[IO, DataSourceTransactor[IO]]
) extends Dao[Address] {

  override val logger = LoggerFactory.getLogger(classOf[AddressService])
  override val tableInfo: TableInfo[Address] = TableInfo.build[Address]()
  override implicit val reader: Read[Address] = Read.derived
  override implicit val writer: Write[Address] = Write.derived

  def findByUserId(
    id: Long
  ): IO[Seq[Address]] = ds.use { xa =>
    val theTableName = Fragment.const0(tableInfo.table.name)
    sql"""
      SELECT * FROM $theTableName
      WHERE person_id = $id
    """.query[Address].to[Seq].transact(xa)
  }
}
