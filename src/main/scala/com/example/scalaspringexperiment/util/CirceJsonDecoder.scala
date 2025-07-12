package com.example.scalaspringexperiment.util

import io.circe.Json
import io.circe.parser
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Decoder
import org.springframework.core.io.buffer.{DataBuffer, DataBufferUtils}
import org.springframework.http.MediaType
import org.springframework.util.MimeType
import reactor.core.publisher.{Flux, Mono}

import java.nio.charset.StandardCharsets
import java.util

class CirceJsonDecoder extends Decoder[io.circe.Json] {

  override def canDecode(
    elementType: ResolvableType,
    mimeType: MimeType
  ): Boolean = {
    mimeType == null || mimeType.isCompatibleWith(MediaType.APPLICATION_JSON)
  }


  /**
   * TODO: there may be performance issues with how this method handles incoming JSON bodies;
   * particularly that it waits for the entire body to be read before decoding it.
   * need to investigate if this is the best approach to use with WebFlux
   *
   * @param input
   * @param elementType
   * @param mimeType
   * @param hints
   * @return
   */
  override def decode(
    input: Publisher[DataBuffer],
    elementType: ResolvableType,
    mimeType: MimeType,
    hints: util.Map[String, AnyRef]
  ): Flux[Json] = {
    DataBufferUtils.join(Flux.from(input)).flatMap { joinedBuffer =>
      val inputStream = joinedBuffer.asInputStream()
      try {
        val jsonStr = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
        parser.decode[Json](jsonStr) match {
          case Right(json) => Mono.just(json)
          case Left(err) => Mono.error(err)
        }
      } finally {
        DataBufferUtils.release(joinedBuffer)
        inputStream.close()
      }
    }.flux()
  }

  override def decodeToMono(
    input: Publisher[DataBuffer],
    elementType: ResolvableType,
    mimeType: MimeType,
    hints: util.Map[String, AnyRef]
  ): Mono[io.circe.Json] = {
    decode(input, elementType, mimeType, hints).single()
  }

  override def getDecodableMimeTypes: util.List[MimeType] =
    util.Arrays.asList(MediaType.APPLICATION_JSON)
}
