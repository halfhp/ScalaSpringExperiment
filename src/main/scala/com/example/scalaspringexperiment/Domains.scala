package com.example.scalaspringexperiment


sealed trait  DomainModel

case class FooDomain(
  val a: String,
  val b: Int
) extends DomainModel

case class BarDomain(
  val c: String,
  val d: Long
) extends DomainModel

case class RestError(
  message: String
)
