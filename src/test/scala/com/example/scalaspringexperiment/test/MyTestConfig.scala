package com.example.scalaspringexperiment.test

import com.example.scalaspringexperiment.SpringConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Profile

import javax.sql.DataSource

@TestConfiguration
@Profile(Array("test"))
class MyTestConfig(
  dataSource: DataSource,
) extends SpringConfig(
  dataSource
) {

  // something that exists in prod that we dont want initializing in our tests
//  @MockBean
//  var someDangerousService: SomeDangerousService = uninitialized

}

