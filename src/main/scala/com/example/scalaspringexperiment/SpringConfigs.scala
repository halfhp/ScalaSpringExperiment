package com.example.scalaspringexperiment

import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class MyConfig {

  @Bean
  def customConverters(): HttpMessageConverters = {
    val circe = new CirceHttpMessageConverter()
    return new HttpMessageConverters(circe)
  }
}

@Configuration
@EnableGlobalMethodSecurity(
  prePostEnabled = true,
  securedEnabled = true
)
class SecurityConfig extends WebSecurityConfigurerAdapter {
  override  def configure(http: HttpSecurity) = {
    http.csrf().disable() // TODO: figure out why this is causing POST to return 403
  }
}
