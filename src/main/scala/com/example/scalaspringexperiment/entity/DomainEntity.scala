package com.example.scalaspringexperiment.entity

import com.example.scalaspringexperiment.dao.{Column, Table}

import java.sql.Timestamp

object DomainEntity {
  val NO_ID = 0L
  val NO_TIMESTAMP = new Timestamp(0)
}

sealed trait DomainEntity {
  val id: Long
  val dateCreated: Timestamp
  val lastUpdated: Timestamp
}

@Table("person")
case class Person(
  @Column("id", true)
  id: Long = DomainEntity.NO_ID,

  @Column("date_created", true)
  dateCreated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("last_updated", true)
  lastUpdated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("name")
  name: String,

  @Column("age")
  age: Int
) extends DomainEntity

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
  street: Option[String],

  @Column("city")
  city: Option[String],

  @Column("state")
  state: Option[String]
) extends DomainEntity

