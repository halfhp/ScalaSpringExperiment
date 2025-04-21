package com.example.scalaspringexperiment

import com.example.scalaspringexperiment.test.MyTestConfig
import org.apache.tomcat.util.bcel.classfile.JavaClass
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.scalatest.*
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestContextManager

import scala.compiletime.uninitialized

@SpringBootTest()
@Import(Array(classOf[MyTestConfig]))
class SimpleControllerTest extends MockitoSugar {

  @Autowired
  var simpleController: SimpleController = uninitialized

  @Autowired
  var simpleService: SimpleService = uninitialized

  @Test
  def testSomething(): Unit = {
    val result = simpleController.test()
    assert(result == "did something then did something else")
  }
}

