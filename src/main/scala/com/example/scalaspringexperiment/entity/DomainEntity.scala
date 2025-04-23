package com.example.scalaspringexperiment.entity

import com.example.scalaspringexperiment.dao.{Column, Table}

import java.sql.Timestamp

object DomainEntity {
  val NO_ID = 0L
  val NO_TIMESTAMP = new Timestamp(0)
}

trait DomainEntity {
  val id: Long
  val dateCreated: Timestamp
  val lastUpdated: Timestamp
}





