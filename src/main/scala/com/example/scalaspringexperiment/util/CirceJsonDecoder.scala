package com.example.scalaspringexperiment.util

import io.circe.Json
import io.circe.parser.decode
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Decoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.{MediaType, ResponseEntity}
import org.springframework.util.MimeType
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

import java.nio.charset.StandardCharsets
import java.util
import java.lang.reflect.Type

class CirceJsonDecoder extends Decoder[io.circe.Json] {

  override def canDecode(elementType: ResolvableType, mimeType: MimeType): Boolean = {
    mimeType == null || mimeType.isCompatibleWith(MediaType.APPLICATION_JSON)
  }

  override def decode(input: Publisher[DataBuffer], elementType: ResolvableType, mimeType: MimeType, hints: util.Map[String, AnyRef]): Flux[io.circe.Json] = {
    Flux.from(input).flatMap { buffer =>
      val jsonStr = StandardCharsets.UTF_8.decode(buffer.asByteBuffer()).toString
      DataBufferUtils.release(buffer)
      io.circe.parser.decode[io.circe.Json](jsonStr) match {
        case Right(json) => Flux.just(json)
        case Left(err)   => Flux.error(new RuntimeException(s"JSON decoding error: ${err.getMessage}"))
      }
    }
  }

  override def decodeToMono(input: Publisher[DataBuffer], elementType: ResolvableType, mimeType: MimeType, hints: util.Map[String, AnyRef]): Mono[io.circe.Json] = {
    decode(input, elementType, mimeType, hints).single()
  }

  override def getDecodableMimeTypes: util.List[MimeType] =
    util.Arrays.asList(MediaType.APPLICATION_JSON)
}
