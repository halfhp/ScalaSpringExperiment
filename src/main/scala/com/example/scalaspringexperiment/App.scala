package com.example.scalaspringexperiment

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.ApplicationListener
import cats.effect.{ExitCode, IO, IOApp}

@SpringBootApplication
class App

object App extends IOApp.Simple {

  /**
   * Main entry point for the application.
   * @return
   */
  def run: IO[Unit] = {
    IO.async_ { cb =>
      val app = new SpringApplication(classOf[App])
      val listener = new ApplicationListener[ContextClosedEvent] {
        override def onApplicationEvent(event: ContextClosedEvent): Unit = cb(Right(ExitCode.Success))
      }
      app.addListeners(listener)
      app.run()
    }
  }
}
