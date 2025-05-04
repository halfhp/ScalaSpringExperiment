package com.example.scalaspringexperiment.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class JwtServerAuthConverter extends ServerAuthenticationConverter {
  override def convert(exchange: ServerWebExchange): Mono[Authentication] = {
    val authHeader = Option(exchange.getRequest.getHeaders.getFirst(HttpHeaders.AUTHORIZATION))
    authHeader match {
      case Some(header) if header.startsWith("Bearer ") =>
        val token = header.stripPrefix("Bearer ").trim
        Mono.just(new UsernamePasswordAuthenticationToken(token, token))
      case _ => Mono.empty()
    }
  }
}