package com.example.scalaspringexperiment

import cats.effect.unsafe.{IORuntime, IORuntimeConfig}
import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.auth.{JwtAuthManager, JwtServerAuthConverter}
import com.example.scalaspringexperiment.util.{CirceJsonDecoder, CirceJsonEncoder}
import doobie.{DataSourceTransactor, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.client.ReactorResourceFactory
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.{SecurityWebFiltersOrder, ServerHttpSecurity}
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.netty.resources.LoopResources

import java.util.concurrent.Executors
import javax.sql.DataSource
import scala.concurrent.ExecutionContext


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
class AsyncConfig {

  /**
   * We limit our cats-effect threadpool to 2 here to correspond to a baseline use case of
   * 2 cpu cores.  This is mainly useful for slightly more consistent benchmarking, though it doesn't
   * control for variations in hardware, other running processes, etc.
   *
   * IMPORTANT: For real-world environments you probably want to just return cats.effect.unsafe.implicits.global
   * which will set the threadpool size the number of available processors.
   *
   * @return
   */
  @Bean
  def catsEffectIORuntime(): IORuntime = {
//    cats.effect.unsafe.implicits.global // use this version to set threadpool size to number of available processors
    val threadPool = Executors.newFixedThreadPool(2)
    val executionContext = ExecutionContext.fromExecutor(threadPool)
    val config = IORuntimeConfig()
    IORuntime(executionContext, executionContext, cats.effect.unsafe.IORuntime.global.scheduler, () => (), config)
  }

  /**
   * More or less the same story here as with cats-effect above.  For real-world environments you can optionally remove this
   * bean completely.  Having said that, all this threadpool is used for is to accept requests and pass them
   * off to the cats-effect threadpool to be processed.  As long as no work that involves blocking IO is done on this threadpool
   * a single thread should generally be sufficient.
   *
   * @return
   */
  @Bean
  def reactorResourceFactory(): ReactorResourceFactory = {
    val factory = new ReactorResourceFactory()
    factory.setUseGlobalResources(false)
    factory.setLoopResources(LoopResources.create("netty", 1, true))
    factory
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

