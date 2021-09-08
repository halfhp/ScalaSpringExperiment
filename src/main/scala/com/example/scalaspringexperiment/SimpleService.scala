package com.example.scalaspringexperiment

import org.springframework.stereotype.Service
import zio.{Task, ZIO}

@Service
class SimpleService {

  def doSomething(): Task[String] = Task {
    "hello"
  }

}
