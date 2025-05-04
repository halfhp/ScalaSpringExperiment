package com.example.scalaspringexperiment

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.auth.{JwtAuthManager, JwtServerAuthConverter}
import com.example.scalaspringexperiment.util.{CirceJsonDecoder, CirceJsonEncoder}
import doobie.{DataSourceTransactor, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.security.authentication.{DelegatingReactiveAuthenticationManager, UsernamePasswordAuthenticationToken}
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.{SecurityWebFiltersOrder, ServerHttpSecurity}
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.{NoOpServerSecurityContextRepository, WebSessionServerSecurityContextRepository}
import org.springframework.web.reactive.config.WebFluxConfigurer

import javax.sql.DataSource


@Configuration
class SpringConfig(
  dataSource: DataSource,
) {

  @Bean
  def doobieTransactor(): Resource[IO, DataSourceTransactor[IO]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
    } yield Transactor.fromDataSource[IO](dataSource, ce)
  }
}

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
  jwtAuthManager: JwtAuthManager,
) {
  @Bean
  def securityFilterChain(
    http: ServerHttpSecurity,
    jwtAuthFilter: AuthenticationWebFilter,
  ): SecurityWebFilterChain = {
    http
      .cors(Customizer.withDefaults())
      .csrf(csrf => csrf.disable())
      .authorizeExchange(_.anyExchange().permitAll())
      .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
      .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // stateless auth
      .build()
  }

    @Bean
    def jwtAuthFilter(
      jwtAuthManager: JwtAuthManager
    ): AuthenticationWebFilter = {
      val filter = new AuthenticationWebFilter(jwtAuthManager)
      filter.setServerAuthenticationConverter(new JwtServerAuthConverter)
      filter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
      filter
    }
}

@Configuration(proxyBeanMethods = false)
class CatsEffectConfig {

  @Bean
  def catsEffectIORuntime(): IORuntime = {
    cats.effect.unsafe.implicits.global
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

