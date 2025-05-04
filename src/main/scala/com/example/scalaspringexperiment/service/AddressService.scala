package com.example.scalaspringexperiment.service

import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.dao.{Dao, TableInfo}
import com.example.scalaspringexperiment.entity.Address
import com.example.scalaspringexperiment.util.PointUtils
import doobie.{DataSourceTransactor, Fragment}
import doobie.implicits.toSqlInterpolator
import doobie.util.{Read, Write}
import doobie.implicits.*
import doobie.postgres.pgisimplicits.PointType
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

  private val theTableName = Fragment.const0(tableInfo.table.name)

  def findByPersonId(
    personId: Long
  ): IO[Seq[Address]] = ds.use { xa =>
    sql"""
      SELECT * FROM $theTableName
      WHERE person_id = $personId
    """.query[Address].to[Seq].transact(xa)
  }

  def findWithinDistance(
    lat: Double,
    lon: Double,
    distanceInMeters: Float
  ): IO[Seq[Address]] = ds.use { xa =>
    val point = PointUtils.pointFromLatLon(lat = lat, lon = lon)
    sql"""
      SELECT * FROM $theTableName
      WHERE st_distancesphere(coordinates, $point) < $distanceInMeters
    """.query[Address].to[Seq].transact(xa)
  }
}
