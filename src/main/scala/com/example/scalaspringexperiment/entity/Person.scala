package com.example.scalaspringexperiment.entity

import com.example.scalaspringexperiment.dao.{Column, Table}

import java.sql.Timestamp

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
