# Overview
A personal exploration into using Spring Framework with Scala 3.

## 4 Years Later...
Since the initial release of Scala 3 I've tried a number of times to upgrade various 
non-trivial personal and professional projects from 2.13 to 3, without success.  Finally in 2025 I succeeded (sort of).  
The jury is still out on whether the upgrade was prudent,
but at least everything is more or less working now.  After getting over the various deprecation hurdles,
next biggest concern has been with the level of compatibility various tools like IntellIJ offer.
There are frequent issues with IDE slowing down to a crawl or getting flat out hung.  Compile times
are also way up and builds will randomly fail, only to succeed on the second or third retry.  

I've updated this projected with a couple items I've learned along the way.

# Libraries and Frameworks Used:

* Spring Boot
* Spring Security
* Circe - JSON serialization and deserialization
* ~~ZIO~~ (Sticking with with Cats Effect out of preference, and because ZIO seems to have been [abandoned by its author](https://degoes.net/articles/splendid-scala-journey).)
* Cats Effect
* [Doobie](https://github.com/typelevel/doobie)[^1]
* Flyway - Database schema definitions and migrations
* ScalaTest
* Mockito

[^1]: So why Doobie and not one of the options that come packaged with Spring?  Two main reasons: 1) Integrates seemlessly with Cats Effect and the IO monad, which is my
preferred tool for structured concurrency.  2) Doobie is oriented around writing pure SQL and producing results as immutable case classes which I prefer over ORM approaches etc. that involve things like Hibernate, JPA, "live objects", etc.

# Challenges & Annoyances
As far as getting Spring and Scala to play nicely together, the challenges are relatively minor and can either be resolved
once in your codebase and forgotten about, or are things that are a just a matter of getting used to.

Probably the most important one to be aware of here relates to async programming.  If you plan to use any form of Scala flavored structured concurrency,
whether it be Futures, Cats Effect, ZIO, or something else, you are going to need to adapt the Spring async programming model to work with it or 
performance will suffer severely.

## Spring's Java Annotations
TODO

## Spring's Threadlocal Context
TODO

## Async Programming 
TODO

# Future Improvements
Things I now know how to do right, that I did not include in this example, or did badly.

## Better Async -> IO coupling in Spring Controllers
TODO

## Fleshed Out DAO
TODO

## More Unit & Integration Test Examples
TODO



