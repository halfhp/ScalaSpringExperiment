package com.example.scalaspringexperiment

import org.springframework.stereotype.Service
import cats.effect.IO
import cats.implicits.*

@Service
class SimpleService {

  case class SimpleServiceError(
    message: String
  )

  def getFoo(): IO[Either[SimpleServiceError, FooDomain]] = IO.pure {
    Either.right(FooDomain(
      a = "hi!",
      b = 2
    ))
  }

  def doSomething(): IO[Either[SimpleServiceError, String]] = IO.pure {
    Either.right("did something")
  }

  def doSomethingElse(): IO[Either[SimpleServiceError, String]] = IO.pure {
    Either.right("did something else")
  }

}
