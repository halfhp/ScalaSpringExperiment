package com.example.scalaspringexperiment.model

import java.time.OffsetDateTime

object DomainModel {
  val NO_ID = 0L
  val NO_TIMESTAMP = OffsetDateTime.MIN
}

sealed trait DomainModel {
  val id: Long
  val dateCreated: OffsetDateTime
  val lastUpdated: OffsetDateTime
}

case class BarDomain(
  override val id: Long = DomainModel.NO_ID,
  override val dateCreated: OffsetDateTime = DomainModel.NO_TIMESTAMP,
  override val lastUpdated: OffsetDateTime = DomainModel.NO_TIMESTAMP,
  val c: String,
  val d: Long
) extends DomainModel

case class FooDomain(
  override val id: Long = DomainModel.NO_ID,
  override val dateCreated: OffsetDateTime = DomainModel.NO_TIMESTAMP,
  override val lastUpdated: OffsetDateTime = DomainModel.NO_TIMESTAMP,
  val a: String,
  val b: Int
) extends DomainModel
