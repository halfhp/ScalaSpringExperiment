package com.example.scalaspringexperiment

import org.springframework.stereotype.Service
import zio.{IO, Task, ZIO}
import zio.config.*

@Service
class SimpleService {

  case class SimpleServiceError(
    message: String
  )

  def getFoo(): IO[SimpleServiceError, FooDomain] = IO.succeed {
    FooDomain(
      a = "hi",
      b = 2
    )
  }

  def doSomething(): IO[SimpleServiceError, String] = IO.succeed {
    "did something"
  }

  def doSomethingElse(): IO[SimpleServiceError, String] = IO.succeed {
    "did something else"
  }

}
