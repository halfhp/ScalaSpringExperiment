package com.example.scalaspringexperiment.util


import io.circe.Json
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Encoder
import org.springframework.core.io.buffer.{DataBuffer, DataBufferFactory}
import org.springframework.http.MediaType
import org.springframework.util.MimeType
import reactor.core.publisher.Flux

import java.nio.charset.StandardCharsets
import java.util

class CirceJsonEncoder extends Encoder[io.circe.Json] {

  override def canEncode(
    elementType: ResolvableType,
    mimeType: MimeType
  ): Boolean =
    mimeType == null || mimeType.isCompatibleWith(MediaType.APPLICATION_JSON)

  override def encode(
    inputStream: Publisher[? <: io.circe.Json],
    bufferFactory: DataBufferFactory,
    elementType: ResolvableType,
    mimeType: MimeType,
    hints: util.Map[String, AnyRef]
  ): Flux[DataBuffer] = {
    Flux.from(inputStream).map { json =>
      val bytes = json.noSpaces.getBytes(StandardCharsets.UTF_8)
      val buffer = bufferFactory.wrap(bytes)
      buffer
    }
  }

  override def encodeValue(
    value: io.circe.Json,
    bufferFactory: DataBufferFactory,
    elementType: ResolvableType,
    mimeType: MimeType,
    hints: util.Map[String, AnyRef]
  ): DataBuffer = {
    val bytes = value.noSpaces.getBytes(StandardCharsets.UTF_8)
    bufferFactory.wrap(bytes)
  }

  override def getEncodableMimeTypes: util.List[MimeType] =
    util.Arrays.asList(MediaType.APPLICATION_JSON)
}
