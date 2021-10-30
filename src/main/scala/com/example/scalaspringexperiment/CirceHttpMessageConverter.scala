package com.example.scalaspringexperiment

import org.springframework.http.HttpOutputMessage
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter

import java.io.{BufferedInputStream, BufferedReader, Reader, Writer}
import java.lang.reflect.Type
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.auto.*

import java.util
import java.util.stream.Collectors

class CirceHttpMessageConverter extends AbstractJsonHttpMessageConverter {

  val CirceJsonType = "io.circe.Json"
  val ObjectMapType = "java.util.Map<java.lang.String, java.lang.Object>"

  override def readInternal(resolvedType: Type, reader: Reader): AnyRef = {
    val br = new BufferedReader(reader)
    val data = br.lines().collect(Collectors.joining()) // TODO: is this stripping newlines?
    parse(data).getOrElse(throw RuntimeException("Invalid JSON"))
  }

  override def writeInternal(obj: Object, t: Type, writer: Writer): Unit = {
    t.getTypeName match {
      case CirceJsonType => writer.write(obj.asInstanceOf[Json].noSpaces)
      case ObjectMapType => {
        val message = obj.asInstanceOf[java.util.Map[String, Object]].get("error").toString
        writer.write(RestError(message).asJson.noSpaces)
      }
      case _ => writer.write(RestError("Something went wrong").asJson.noSpaces)
    }
  }

}
