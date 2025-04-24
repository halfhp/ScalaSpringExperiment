package com.example.scalaspringexperiment

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.util.{CirceJsonDecoder, CirceJsonEncoder}
import doobie.{DataSourceTransactor, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.WebFluxConfigurer

import javax.sql.DataSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SpringConfig(
  dataSource: DataSource,
) {

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
  def securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = {
    http
      .cors(Customizer.withDefaults())
      .csrf(csrf => csrf.disable()) // Stateless app using JWT
      .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // optional, disables session caching
      .authorizeExchange(authz =>
        authz.anyExchange().permitAll()
      )
//      .httpBasic().disable() // or leave enabled if using basic auth
//      .formLogin().disable()
      .build()
  }
}

@Configuration
class CirceWebFluxConfig extends WebFluxConfigurer {

  override def configureHttpMessageCodecs(configurer: ServerCodecConfigurer): Unit = {
    // disable jackson codecs
    configurer.defaultCodecs().jackson2JsonDecoder(null)
    configurer.defaultCodecs().jackson2JsonEncoder(null)

    // register circe codecs
    configurer.customCodecs().register(new CirceJsonDecoder())
    configurer.customCodecs().register(new CirceJsonEncoder())
  }
}

