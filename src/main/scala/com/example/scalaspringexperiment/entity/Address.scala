package com.example.scalaspringexperiment.entity

import com.example.scalaspringexperiment.dao.{Column, Table}
import net.postgis.jdbc.geometry.Point

import java.sql.Timestamp

@Table("address")
case class Address(
  @Column("id", true)
  id: Long = DomainEntity.NO_ID,

  @Column("date_created", true)
  dateCreated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("last_updated", true)
  lastUpdated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("person_id")
  personId: Long,

  @Column("street")
  street: Option[String] = None,

  @Column("city")
  city: Option[String] = None,

  @Column("state")
  state: Option[String] = None,

  @Column("coordinates")
  coordinates: Option[Point] = None,
) extends DomainEntity
