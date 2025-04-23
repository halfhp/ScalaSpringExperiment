package com.example.scalaspringexperiment

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.util.CirceHttpMessageConverter
import doobie.{DataSourceTransactor, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

import javax.sql.DataSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SpringConfig(
  dataSource: DataSource,
) {

  @Bean
  def getCustomConverters(): HttpMessageConverters = {
    val circe = new CirceHttpMessageConverter()
    new HttpMessageConverters(circe)
  }

  @Bean
  def catsEffectIORuntime(): IORuntime = {
    cats.effect.unsafe.implicits.global
  }

  @Bean
  def doobieTransactor(): Resource[IO, DataSourceTransactor[IO]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
    } yield Transactor.fromDataSource[IO](dataSource, ce)
  }

  @Bean
  def securityFilterChain(
    http: HttpSecurity
  ): SecurityFilterChain = {
    http
      .cors(Customizer.withDefaults())

      // we'll be using stateless JWT authentication, and csrf messes with mockmvc tests
      // so we're disabling csrf.  alternatively, this could be disabled only for testing
      // in the test config.
      .csrf(csrf => csrf.disable())
      .sessionManagement(sm => sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .build()
  }
}

