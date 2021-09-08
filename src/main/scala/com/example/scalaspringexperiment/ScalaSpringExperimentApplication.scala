package com.example.scalaspringexperiment

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ScalaSpringExperimentApplication

object ScalaSpringExperimentApplication {

  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[ScalaSpringExperimentApplication])
  }
}
