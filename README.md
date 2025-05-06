[![build](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml/badge.svg)](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml)
[![Codix](https://codix.io/gh/badge/halfhp/ScalaSpringExperiment)](https://codix.io/gh/repo/halfhp/ScalaSpringExperiment)

:speech_balloon: **Questions / comments / suggestions are welcome in the [discussions](https://github.com/halfhp/ScalaSpringExperiment/discussions), or feel free to [contact me](mailto:halfhp@gmail.com) directly.**
# Overview
Demonstrates using Spring Framework with Scala 3.  

This codebase attempts to provide a sample project that includes
working implementations of the most commonly used elements in a performant modern REST API (authn/authz, JWT, async programming, 
unit & integration tests, etc.) while remaining as simple and easy to understand as possible.

## Why Scala?
There probably isn't a universally great answer.  Anyone checking out this codebase probably already has their own
valid reasons for wanting to use Scala. I will say that while the learning curve may be high, assuming a team of strong developers,
Scala is certainly one of if not the most productive JVM languages to exist.

Everyone else interested in using Spring with something other than Java, consider using Kotlin instead.
It's no secret that the design of Kotlin draws quite a bit of inspiration from Scala, and more recently, Scala3 has begun to take inspiration from Kotlin as well,
so there is some convergence happening.  Throw in a functional programming library like [Arrow](https://arrow-kt.io/) and you have a compelling alternative
that plays a little better with Spring's Java codebase, has a lower learning curve and a larger pool of active developers.  
The biggest gripe I have with Kotlin when it comes to backend systems has to do with database libraries.  
There are many to choose from, but due to laguage limitations (last I checked) do not offer the same combination of
expressiveness, functionality, and type safety of something like Doobie. How big of a deal that really is depends on the project and the team.

## Why Scala3?
The biggest headaches of moving from Scala 2.x to Scala 3 have been with the level of compatibility tools like IntellIJ offer, and the relatively small market share
it has of Scala projects and libraries. Builds sometimes slow to a crawl, hang, or randomly fail, only to succeed after a second or third retry.

The jury is still out on whether the migration is worthwhile for established projects, but I do feel that Scala3 has reached the point
where it is the better choice for new projects.

# How to Run
For convenience this project includes a [docker-compose environment](docker-compose.yml) that provisions a postgres database and configures
the app to use it.

There is also a K6 benchmark jetpack-compose environment in `/extras` that is preconfigured to run against localhost:8080.
See the [README](extras/k6/README.md) for more information.

![K6 Benchmark Dashboard](./docs/k6-dashboard.png)

# Libraries and Frameworks Used

* Gradle[^1]
* Spring Boot
* Spring Security
* [JWT Scala](https://github.com/jwt-scala/jwt-scala)
* [Circe](https://github.com/circe/circe) - JSON serialization and deserialization
* ~~ZIO~~ (Sticking with with Cats Effect out of preference, and because ZIO seems to have been [abandoned by its author](https://degoes.net/articles/splendid-scala-journey).)
* Cats Effect
* [Doobie](https://github.com/typelevel/doobie)[^2]
* Flyway - Database schema definitions and migrations
* ScalaTest
* Mockito

[^1]: I've used for several Scala projects now and have few regrets. SBT I believe might have some minor performance benefits, 
but you really cant beat Gradle in terms of features and support.  Having said that, it is possible that some of the build
instability / Intellij bugginess I am experiencing is due to Gradle.

[^2]: So why Doobie and not one of the options that come packaged with Spring?  Two main reasons: 1) Integrates seemlessly with Cats Effect and the IO monad, which is my
preferred tool for structured concurrency.  2) Doobie is oriented around writing pure SQL and producing results as immutable case classes which I prefer over ORM approaches etc. that involve things like Hibernate, JPA, "live objects", etc.

# Challenges & Annoyances
As far as getting Spring and Scala to play nicely together, the challenges are fairly minor and can either be resolved
once in your codebase and forgotten about, or are things that just take time getting used to.

The big one one to be aware of here relates to async programming.  If you plan to use any form of Scala flavored structured concurrency,
whether it be Futures, Cats Effect, ZIO, or something else, you need to adapt the Spring async programming model to work with it or 
performance will suffer.

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

Watch out for this when using Spring's `@RequestParam` and `@PathVariable` annotations in controllers.

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

### Why not use Reactor?
Anecdotally, I soured on RxJava quite some time ago due what I consider to be poor design.  The complete laundry list is out of scope
but by way of a couple examples, no common base for reactive types, and the inability to emit null values. 

While they share similar syntax, Reactor is not RxJava, and even fixes some of its mistakes.  From a developer productivity
perspective I find many other tools to be more expressive, less "magical" and more extensible; 
cats-effect, ZIO, and Kotlin coroutines with Flow (if you use Kotlin) 
to name a few.

### Async Controllers
The original version of this project used WebMVC which is built on top of Apache Tomcat and has pseudo-async support.
I've since switched to using WebFlux which uses Netty and is generally more performant, particularly when under heavy load.  
I would not be surprised if this changes in the future thanks to the work being done on Project Loom.  
For those interested in exploring this further, check out the old [webmvc tag](https://github.com/halfhp/ScalaSpringExperiment/releases/tag/webmvc)of this repository.

### Async Database Drivers
This project uses Doobie, which is built on top of JDBC which is synchronous but supports a wide variety SQL databases.  
There is another library, Skunk, which is written by the same author and offers similar functionality.  
It's fully asynchronous but locks you into Postgres.

Another option would be to use [R2DBC](https://spring.io/projects/spring-data-r2dbc) which is also async.  I've not tried this approach
yet but imagine it could be wrapped with cats-effect IO similarly to what was done with [Mono] in the controller layer.

# Issues
## IO[T] to Mono[T] Conversion
Right now this conversion is done in the controller layer like this:
```scala
private given ioToMono[A](): Conversion[IO[A], Mono[A]] with {
    def apply(io: IO[A]): Mono[A] = {
        Mono.fromFuture(new CompletableFuture[A]().tap { cf =>
            io.unsafeRunAsync {
              case Right(value) => cf.complete(value)
              case Left(error) => cf.completeExceptionally(error)
            }
        })
    }
}
```
There may be performance issues going between the WebFlux and Cats Effect threadpools, particularly when
running in an environment with a small number of CPU cores.  This has to do with how time sharing is handled with fibers / virtual threads;
in Cats Effect, a fiber generally [only yields on an IO boundary](https://typelevel.org/cats-effect/docs/2.x/datatypes/io), or in other words, when one of it's composite IO blocks completes.
This is fundamentally different from true threads where the OS scheduler ensures each thread gets a fair amount of CPU time regardless of
where / what each thread is doing.

Ideally there is at least one CPU core dedicated to each thread in each pool, but I am not sure how thread affinity works here
or if it is even possible to exclusively bind a CPU to each of these threads.  I also plan to provide better detail on
what happens under load in situations where there are fewer cores than threads, or even a single core.

# Future Improvements
## Spring Security
* Add OAuth2 request/ refresh tokens
* Add Oauth2 client to support third party authentication (Google, etc)
* Add JTI to JWT tokens to support revocation




