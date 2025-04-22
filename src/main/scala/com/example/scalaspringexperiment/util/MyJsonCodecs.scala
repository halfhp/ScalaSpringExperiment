package com.example.scalaspringexperiment.util

import io.circe.{Codec, Encoder, HCursor}

import java.sql.Timestamp
import java.time.Instant

object MyJsonCodecs {
  implicit val timestampCodec: Codec[Timestamp] = Codec.from(
    decodeA = (c: HCursor) => {
      c.value match {
        case v if v.isString => v.as[String].map(str => Timestamp.from(Instant.parse(str)))
        case v if v.isNumber => v.as[Long].map(l => new Timestamp(l))
        case _ => ???
      }
    },
    encodeA = Encoder.encodeLong.contramap[Timestamp](_.getTime)
  )
}
