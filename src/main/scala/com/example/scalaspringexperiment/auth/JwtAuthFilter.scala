package com.example.scalaspringexperiment.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.{ServerWebExchange, WebFilter, WebFilterChain}
import reactor.core.publisher.Mono


@Component
class JwtAuthFilter(
  authManager: JwtAuthManager
) extends WebFilter {
  override def filter(
    exchange: ServerWebExchange,
    chain: WebFilterChain
  ): Mono[Void] = {
    val authHeader = Option(exchange.getRequest.getHeaders.getFirst(HttpHeaders.AUTHORIZATION))
    authHeader match {
      case Some(header) if header.startsWith("Bearer ") =>
        val token = header.substring(7)
        val auth = new UsernamePasswordAuthenticationToken(token, token)
        authManager.authenticate(auth).doOnNext(a => SecurityContextHolder.getContext.setAuthentication(a)).`then`(chain.filter(exchange))
      case _ =>
        chain.filter(exchange)
    }
  }
}
