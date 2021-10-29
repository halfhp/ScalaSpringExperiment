package com.example.scalaspringexperiment

import org.springframework.stereotype.Service
import zio.{Task, ZIO, IO}
import zio.config.*

@Service
class SimpleService {

  case class SimpleServiceError(
    message: String
  )

  def doSomething(): IO[SimpleServiceError, String] = IO.succeed {
    "did something"
  }

  def doSomethingElse(): IO[SimpleServiceError, String] = IO.succeed {
    "did something else"
  }

}
