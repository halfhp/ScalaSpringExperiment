[![build](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml/badge.svg)](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml)
[![Codix](https://codix.io/gh/badge/halfhp/ScalaSpringExperiment)](https://codix.io/gh/repo/halfhp/ScalaSpringExperiment)
# Overview
Demonstrates using Spring Framework with Scala 3.

The biggest headaches of upgrading from Scala 2.13 to Scala 3 has been with the level of compatibility tools like IntellIJ offer, and the relatively small market share
it has of Scala projects and libraries. Builds sometimes slow to a crawl or hang or randomly fail, only to succeed after a second or third retry.

The jury is still out on whether the migration is worthwhile for established projects, but
as of 2025 I do feel like Scala 3 is the way to go for new projects.

:speech_balloon: **Questions / comments / suggestions are welcome in the [discussions](https://github.com/halfhp/ScalaSpringExperiment/discussions), or feel free to [contact me](mailto:halfhp@gmail.com) directly.**

# How to Run
For convenience this project includes a [docker-compose environment](docker-compose.yml) that provisions a postgres database and configures
the app to use it.

# Libraries and Frameworks Used

* Gradle[^1]
* Spring Boot
* Spring Security
* Circe - JSON serialization and deserialization
* ~~ZIO~~ (Sticking with with Cats Effect out of preference, and because ZIO seems to have been [abandoned by its author](https://degoes.net/articles/splendid-scala-journey).)
* Cats Effect
* [Doobie](https://github.com/typelevel/doobie)[^2]
* Flyway - Database schema definitions and migrations
* ScalaTest
* Mockito

[^1]: I chose Gradle over SBT initially out of curiosity.  At this point I've used for several Scala projects now and have no regrets.
SBT I believe might have some minor performance benefits, but you really cant Gradle in terms of features and support.

[^2]: So why Doobie and not one of the options that come packaged with Spring?  Two main reasons: 1) Integrates seemlessly with Cats Effect and the IO monad, which is my
preferred tool for structured concurrency.  2) Doobie is oriented around writing pure SQL and producing results as immutable case classes which I prefer over ORM approaches etc. that involve things like Hibernate, JPA, "live objects", etc.

# Challenges & Annoyances
As far as getting Spring and Scala to play nicely together, the challenges are fairly minor and can either be resolved
once in your codebase and forgotten about, or are things that are a just take some getting used to.

Probably the most important one to be aware of here relates to async programming.  If you plan to use any form of Scala flavored structured concurrency,
whether it be Futures, Cats Effect, ZIO, or something else, you are going to need to adapt the Spring async programming model to work with it or 
performance will suffer severely.

## Java -> Scala Interop
Since we're using Scala and Spring is written in Java, we have to handle some idiosyncrasies.  These are a few of the most
common ones I have run into.

### Annotations
Since Spring and JUnit rely heavily on annotations, this is going to be one that you will run into a lot.

A REST endpoint in Java:
```java
@GetMapping(path = "/test")
public JsonNode test() {...}
```

Looks like this in Scala:
```scala
@GetMapping(path = Array("/test"))
def test(): String = {...}
```

### Collections
Java and Scala collections are generally not interchangeable.  In most cases handling this is as simple
as importing the necessary converter and using it.  [More info available here](https://docs.scala-lang.org/overviews/collections/conversions-between-java-and-scala-collections.html).

### Null Values
As of this writing and so far as I am aware, Spring still uses null instead of [Java 8's Optional monad](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).
This means that whenever you are working with variables coming Spring, you generally want to wrap them in a Scala Option.

One particular place to watch out for this is when using Spring's `@RequestParam` and `@PathVariable` annotations in controllers.

## Spring's ThreadLocal Context
Parts of Spring's architecture relies on ThreadLocal context, particularly when using WebMVC.
This can be problematic when interfacing between things like controller entry points and services and utilities that are built
around IO/Future/ZIO etc. monads.  Effectively, trying to access something like Spring Security's SecurityContext from these methods
will not work.  Without going into too much detail WebFlux has the same basic problem, even though its not technically using ThreadLocal context.

My preferred solution is to pass the SecurityContext and any other ThreadLocal / pseudo global context data as an argument to
these methods.

## Async Programming 
Spring has it's own mechanisms for async programming, and it takes some work to adapt it to be compatible with IO monads.
Even after adapting these mechanisms we are left with having to manage an additional threadpool(s) to accommodate Spring.
Another challenge here is adapting the handling of uncaught exceptions so that Spring's conventional mechanisms will 
continue to function.

### Async Controllers
The original version of this project used WebMVC which is built on top of Apache Tomcat and has its own async programming model.
I've since switched to using WebFlux which is built on top of Netty and is generally considered to be more performant, particularly
when it comes to servicing large numbers of requests concurrently.  I would not be surprised if this changes in the future
thanks to the work being done on Project Loom.  For those interested in exploring this further, check out the [webmvc tag](https://github.com/halfhp/ScalaSpringExperiment/releases/tag/webmvc)
of this repository.

### Async Database Drivers
This project uses Doobie, which is built on top of JDBC which is synchronous.  There is another library, Skunk, which is written
by the same author and offers similar functionality.  It's fully asynchronous but also locks you into using Postgres.

Another option would be to use one Spring's database facilities that supports R2DBC, which is also async.  I've not tried this approach
yet but imagine it could be wrapped with cats-effect IO similarly to what was done with [Mono] in the controller layer.

# Future Improvements
## Spring Security
In particular, add JTW authentication and secured endpoints + tests

## Async Rest Controller
Create an AsyncController that demonstrates adapting Spring's async programming model
to work effectively with Cats Effect IO's.




