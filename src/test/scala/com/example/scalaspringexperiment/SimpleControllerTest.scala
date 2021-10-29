package com.example.scalaspringexperiment

import org.apache.tomcat.util.bcel.classfile.JavaClass
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.scalatest.*
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestContextManager

@SpringBootTest
class SimpleControllerTest extends featurespec.AnyFeatureSpec with MockitoSugar {

  @Autowired
  val simpleController: SimpleController = null

  @Autowired
  val simpleService: SimpleService = null

  // TODO - figure out how to get rid of this
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  Feature("A Feature") {
    Scenario("A Scenario") {
      val w = simpleController.test()
      assert(true)
    }
  }

}

