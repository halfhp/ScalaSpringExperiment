package com.example.scalaspringexperiment.test

import com.example.scalaspringexperiment.SpringConfig
import org.scalatestplus.mockito.MockitoSugar.mock
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.test.context.TestConfiguration
//import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.core.env.Environment
import org.springframework.test.context.bean.`override`.mockito.MockitoBean

import javax.sql.DataSource
import scala.compiletime.uninitialized

@TestConfiguration
@Profile(Array("test"))
class MyTestConfig(
  //environment: Environment,
  //logFacade: LogFacade,
  dataSource: DataSource,
) extends SpringConfig(
  dataSource
) {

//  @Bean
//  override def getCustomConverters(): HttpMessageConverters = {
//    val circe = new CirceHttpMessageConverter(isPrettyPrintEnabled = true)
//    new HttpMessageConverters(circe)
//  }

  // something that exists in prod that we dont want initializing in our tests
//  @MockBean
//  var someDangerousService: SomeDangerousService = uninitialized

}

