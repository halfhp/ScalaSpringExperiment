package com.example.scalaspringexperiment.util

import io.circe.{Json, JsonObject}

object JsonUtils {

  extension (json: Json)
    def stripFields(fields: String*): Json = {
      def loop(j: Json): Json = {
        j.mapObject { obj =>
          JsonObject.fromIterable {
            obj.toIterable.collect {
              case (k, v) if !fields.contains(k) => k -> loop(v)
            }
          }
        }.mapArray(_.map(loop))
      }

      loop(json)
    }
}
