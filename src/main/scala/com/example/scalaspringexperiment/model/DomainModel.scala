package com.example.scalaspringexperiment.model

import java.sql.Timestamp
import java.time.OffsetDateTime

object DomainModel {
  val NO_ID = 0L
  //val NO_TIMESTAMP = OffsetDateTime.MIN
  val NO_TIMESTAMP = new Timestamp(0)
}

sealed trait DomainModel {
  val id: Long
  val dateCreated: Timestamp
  val lastUpdated: Timestamp
}

case class BarDomain(
  id: Long = DomainModel.NO_ID,
  dateCreated: Timestamp = DomainModel.NO_TIMESTAMP,
  lastUpdated: Timestamp = DomainModel.NO_TIMESTAMP,
  c: String,
  d: Long
) extends DomainModel

case class FooDomain(
  id: Long = DomainModel.NO_ID,
  dateCreated: Timestamp = DomainModel.NO_TIMESTAMP,
  lastUpdated: Timestamp = DomainModel.NO_TIMESTAMP,
  a: String,
  b: Int
) extends DomainModel
