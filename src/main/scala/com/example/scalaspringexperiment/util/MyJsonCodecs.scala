package com.example.scalaspringexperiment.util

import io.circe.{Codec, Encoder, HCursor, Json}
import net.postgis.jdbc.geometry.Point

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

  implicit val pointCodec: Codec[Point] = Codec.from(
    decodeA = (c: HCursor) => {
      c.value match {
        case v if v.isObject =>
          for {
            lat <- v.hcursor.get[Double]("lat")
            lon <- v.hcursor.get[Double]("lon")
          } yield new Point(lat, lon)
        case _ => ???
      }
    },
    encodeA = Encoder.encodeJson.contramap[Point](p => Json.obj(
      "lat" -> Json.fromDoubleOrNull(p.getX),
      "lon" -> Json.fromDoubleOrNull(p.getY),
    ))
  )
}
