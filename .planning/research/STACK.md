# Stack Research: Lila Analysis API

**Domain:** Scala-based Chess Analysis API (Lichess core integration)
**Researched:** 2025-03-01
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| **Scala** | **3.3.5 (LTS)** | Language | Current Lichess standard. Provides type-safe enums and opaque types ideal for chess domain modeling. |
| **Akka HTTP** | **10.2.10** | Web Server | The last Apache 2.0 version. High-performance, lean, and matches the Akka 2.6 ecosystem used in Lichess modules. |
| **Tapir** | **1.10.15** | API DSL | Declarative, type-safe API definitions. Generates OpenAPI/Swagger docs automatically, making the Analysis API easy to consume. |
| **Akka** | **2.6.21** | Runtime | Matches the exact version used in the provided `lila` modules. Ensures binary compatibility for Actor and Stream integration. |
| **Stockfish** | **16.1+** | Engine | The industry standard for chess analysis, used by Lichess for all server-side evaluations. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| **Play-JSON** | **3.0.6** | Serialization | **Mandatory.** Used by `lila.common`, `scalachess`, and `lila.analyse` for all JSON data formats. |
| **Macwire** | **2.6.7** | DI | Use for compile-time dependency injection to maintain consistency with Lichess's architecture. |
| **Scaffeine** | **5.3.0** | Caching | Recommended for in-memory caching of analysis results (e.g., caching Stockfish evals for common positions). |
| **Scalalib** | **11.9.5** | Core Utils | Lichess's standard utility library. Provides essential extensions for `Option`, `Future`, and custom types. |
| **Scalachess** | **17.14.3** | Chess Logic | The backbone of Lichess's move validation, PGN parsing, and board representation. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| **sbt 1.10+** | Build Tool | Standard for Scala. Required for managing complex multi-module dependencies of Lichess. |
| **Docker** | Deployment | Essential for bundling Stockfish binaries with the Scala application for consistent analysis results. |

## Installation

```scala
// build.sbt additions for the API module
val tapirVersion = "1.10.15"
val akkaHttpVersion = "10.2.10"
val akkaVersion = "2.6.21"

libraryDependencies ++= Seq(
  // Tapir with Akka HTTP (using for3Use2_13 for Akka compatibility in Scala 3)
  ("com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion).cross(CrossVersion.for3Use2_13),
  ("com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion),
  ("com.softwaremill.sttp.tapir" %% "tapir-json-play" % tapirVersion),
  
  // Akka stack (matched to lila core)
  ("com.typesafe.akka" %% "akka-http" % akkaHttpVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-stream" % akkaVersion).cross(CrossVersion.for3Use2_13),
  
  // Lichess Core (Assuming they are available in local/remote maven)
  "org.lichess" %% "scalalib-core" % "11.9.5",
  "org.lichess" %% "scalachess" % "17.14.3"
)
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| **Akka HTTP** | **Play Framework** | If the API needs to serve HTML/UI components or requires heavy session management. |
| **Akka HTTP** | **Apache Pekko** | If you want to move away from Akka entirely. Recommended only if *all* Lichess modules are also migrated to Pekko. |
| **Tapir** | **Pekko HTTP (Native)** | If building a pure Scala 3 service from scratch without Akka 2.6 dependencies. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| **Akka 2.7+** | Licensing changed to BSL (non-free for many users). | Akka 2.6.21 (OSS) or Apache Pekko. |
| **Circe / ZIO-JSON** | Breaks compatibility with Lichess's pervasive `Play-JSON` formats used in `analyse` and `game` modules. | Play-JSON 3.0.x. |
| **ZIO / Cats Effect** | Introduces a functional paradigm shift that makes direct integration with Lichess's `Future`-heavy codebase friction-prone. | Standard `scala.concurrent.Future` with Akka Streams. |

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| **Tapir 1.10.x** | **Akka HTTP 10.2.x** | Requires `CrossVersion.for3Use2_13` in Scala 3 projects due to Akka's Scala 2 legacy. |
| **Play-JSON 3.0.x** | **Scala 3.3.x** | Fully compatible and recommended over the older 2.x branch for Scala 3 projects. |
| **Akka 2.6.21** | **JDK 17/21** | Fully supported. Ensure `--enable-native-access` if using newer JVM features. |

## Sources

- `project/Dependencies.scala` — Verified Lichess-specific library versions (Akka 2.6.21, Play-JSON 3.0.6).
- [Official Tapir Docs](https://tapir.softwaremill.com/) — Verified Akka HTTP interpreter support for Scala 3.
- [Lichess GitHub](https://github.com/lichess-org/lila) — Verified current engine and framework directions.
- `google_web_search` — Confirmed Akka vs Pekko migration status in the Lichess ecosystem.

---
*Stack research for: Lila Analysis API*
*Researched: 2025-03-01*
