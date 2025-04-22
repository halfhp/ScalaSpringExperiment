package com.example.scalaspringexperiment.model

import com.example.scalaspringexperiment.dao.{Column, Table}

import java.sql.Timestamp
import java.time.OffsetDateTime

object DomainModel {
  val NO_ID = 0L
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

@Table("foo")
case class FooDomain(

  @Column("id", true)
  id: Long = DomainModel.NO_ID,

  @Column("date_created")
  dateCreated: Timestamp = DomainModel.NO_TIMESTAMP,

  @Column("last_updated")
  lastUpdated: Timestamp = DomainModel.NO_TIMESTAMP,

  @Column("a")
  a: String,

  @Column("b")
  b: Int
) extends DomainModel

