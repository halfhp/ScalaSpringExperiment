package com.example.scalaspringexperiment

import org.springframework.boot.{ExitCodeGenerator, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.{ApplicationEvent, ApplicationListener}
import cats.effect.{ExitCode, IO, IOApp}
import org.springframework.stereotype.Component

@SpringBootApplication
class ScalaSpringExperimentApplication

object ScalaSpringExperimentApplication extends IOApp.Simple {

  def run = {
    IO.async_ { cb =>
        val app = SpringApplication.run(classOf[ScalaSpringExperimentApplication])
        val listener = new ApplicationListener[ContextClosedEvent] {
          override def onApplicationEvent(event: ContextClosedEvent) = cb(Right(ExitCode.Success))
        }

        app.addApplicationListener(listener)
    }
  }

}
