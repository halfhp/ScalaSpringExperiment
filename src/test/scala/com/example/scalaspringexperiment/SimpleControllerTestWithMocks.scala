package com.example.scalaspringexperiment

import org.apache.tomcat.util.bcel.classfile.JavaClass
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.{InjectMocks, Mock}
import org.mockito.Mockito.{when}
import org.scalatest.*
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import zio.{Task, ZIO, IO}

class SimpleControllerTestWithMocks extends featurespec.AnyFeatureSpec with MockitoSugar {

  @InjectMocks
  val simpleController: SimpleController = null

  @Mock
  val simpleService: SimpleService = null

  // TODO - figure out how to get rid of this
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  Feature("A Feature") {
    Scenario("A Scenario") {

      when(simpleService.doSomething()).thenReturn {
        IO.succeed("first")
      }

      when(simpleService.doSomethingElse()).thenReturn {
        IO.succeed("second")
      }

      assert(simpleController.test() == "first then second")
    }

    Scenario("Another Scenario") {
//      when(simpleService.getFoo()).thenReturn {
//        IO.succeed(FooDomain("aval", 2))
//      }
//      val json = simpleController.getFoo()
//      assert(json == "{\"a\":\"aval\",\"b\":2}")
    }
  }
}

