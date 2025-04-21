package com.example.scalaspringexperiment.util

import com.example.scalaspringexperiment.util.CirceHttpMessageConverter.{CirceJsonType, ObjectMapType}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter

import java.io.{BufferedReader, Reader, Writer}
import java.lang.reflect.Type
import java.util
import java.util.stream.Collectors

object CirceHttpMessageConverter {
  val CirceJsonType = "io.circe.Json"
  val ObjectMapType = "java.util.Map<java.lang.String, java.lang.Object>"
}

/**
 * Custom HTTP message converter for Circe JSON serialization/deserialization.
 */
class CirceHttpMessageConverter extends AbstractJsonHttpMessageConverter {

  override def readInternal(resolvedType: Type, reader: Reader): AnyRef = {
    val br = new BufferedReader(reader)
    val data = br.lines().collect(Collectors.joining()) // TODO: is this stripping newlines?
    parse(data).getOrElse(throw RuntimeException("Invalid JSON"))
  }

  override def writeInternal(obj: Object, t: Type, writer: Writer): Unit = {
    t.getTypeName match {
      case CirceJsonType => writer.write(obj.asInstanceOf[Json].noSpaces)
      case ObjectMapType =>
        val message = obj.asInstanceOf[java.util.Map[String, Object]].get("error").toString
        writer.write(RestError(message).asJson.noSpaces)
      case _ => writer.write(RestError("Something went wrong").asJson.noSpaces)
    }
  }

}
