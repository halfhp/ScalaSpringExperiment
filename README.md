[![build](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml/badge.svg)](https://github.com/halfhp/ScalaSpringExperiment/actions/workflows/build.yml)
[![Codix](https://codix.io/gh/badge/halfhp/ScalaSpringExperiment)](https://codix.io/gh/repo/halfhp/ScalaSpringExperiment)
# Overview
A personal exploration into using Spring Framework with Scala 3.

## 4 Years Later...
Since the initial release of Scala 3 I've tried a number of times to upgrade various 
non-trivial personal and professional projects from 2.13 to 3, without success.  Finally in 2025 I succeeded (sort of).  
The jury is still out on whether the upgrade was prudent,
but at least everything is more or less working now.  After getting over the various deprecation hurdles,
next biggest concern has been with the level of compatibility various tools like IntellIJ offer.
There are frequent issues with IDE slowing down to a crawl or getting flat out hung.  Compile times
are also way up and builds randomly fail, after a second or third retry.  

I've updated this projected with a couple items I've learned along the way.

# Libraries and Frameworks Used:

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
Much of Spring's async programming model relies on ThreadLocal context.  This used to be a common pattern in Java, but not one that is used in Scala.
This becomes particularly annoying when interfacing between things like controller entry points and services and utilities that are built
around IO/Future/ZIO etc. monads.  Effectively, trying to access something like Spring Security's SecurityContext from these methods
will not work.  The best solution I have found is to pass the SecurityContext and any other ThreadLocal context as an argument to
these methods.  This is not ideal, but it is the best solution I have found so far.

## Async Programming 
Spring has it's own mechanisms for async programming, and it takes some work to adapt it to be compatible with IO monads.
Even after adapting these mechanisms we are left with having to manage an additional threadpool to accommodate Spring.
The other challenge here is adapting the handling of uncaught exceptions so that Spring's conventional mechanisms will 
continue to function.

I've not gotten around to adding this to the example yet, but it is doable, and once it's done you can pretty much forget
about it.

# Future Improvements
## Things I know how to do but have not gotten around to

### Async -> IO conversions in Rest controllers
TODO

## Things I dont know how to do yet
TODO



