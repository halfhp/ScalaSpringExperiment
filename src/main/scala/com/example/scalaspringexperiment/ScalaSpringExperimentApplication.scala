package com.example.scalaspringexperiment

import org.springframework.boot.{ExitCodeGenerator, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.{ApplicationEvent, ApplicationListener}
import zio.Task
import zio.ZIO
import zio.IO

@SpringBootApplication
class ScalaSpringExperimentApplication

object ScalaSpringExperimentApplication extends zio.App {

  def run(args: List[String]) = {
    IO.effectAsync { cb =>
      val app = SpringApplication.run(classOf[ScalaSpringExperimentApplication])
      val listener = new ApplicationListener[ContextClosedEvent] {
        override def onApplicationEvent(event: ContextClosedEvent) = cb(IO.unit)
      }
      app.addApplicationListener(listener)
    }.exitCode
  }

}
