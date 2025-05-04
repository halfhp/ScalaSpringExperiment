package com.example.scalaspringexperiment.entity

import com.example.scalaspringexperiment.dao.{Column, Table}

import java.sql.Timestamp

@Table("registered_user")
case class RegisteredUser(
  @Column("id", true)
  id: Long = DomainEntity.NO_ID,

  @Column("date_created", true)
  dateCreated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("last_updated", true)
  lastUpdated: Timestamp = DomainEntity.NO_TIMESTAMP,

  @Column("email")
  email: String,

  @Column("email_verified")
  emailVerified: Boolean = false,

  @Column("roles")
  roles: List[String],

  @Column("password_hash")
  passwordHash: String,
  
  @Column("person_id")
  personId: Long

) extends DomainEntity
