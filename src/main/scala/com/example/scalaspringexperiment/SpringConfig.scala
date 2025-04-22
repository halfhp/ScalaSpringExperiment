package com.example.scalaspringexperiment

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import com.example.scalaspringexperiment.util.CirceHttpMessageConverter
import doobie.{DataSourceTransactor, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.{Bean, Configuration, Lazy}

import javax.sql.DataSource

@Configuration
class SpringConfig(
  dataSource: DataSource,
) {

//  @Bean
//  def customConverters(): HttpMessageConverters = {
//    val circe = new CirceHttpMessageConverter()
//    new HttpMessageConverters(circe)
//  }

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

//  @Bean
//  def dataSource(): DataSource = {
//    DataSourceBuilder.create()
//      .driverClassName("org.postgresql.Driver")
//      .url("jdbc:postgresql://localhost:5432/springtest")
//      .username("postgres")
//      .password("ou812")
//      .build()
//  }
}

// TODO: fixme
//@Configuration
//@EnableGlobalMethodSecurity(
//  prePostEnabled = true,
//  securedEnabled = true
//)
//class SecurityConfig extends WebSecurityConfigurerAdapter {
//  override  def configure(http: HttpSecurity) = {
//    http.csrf().disable() // TODO: figure out why this is causing POST to return 403
//  }
//}

