# Phase 1: Foundation for Lila Analysis API - Research

**Researched:** 2026-03-02
**Domain:** Scala/Play, Stockfish UCI, sbt Build System
**Confidence:** MEDIUM

## Summary
The Lila codebase is a modular Scala/Play application using Scala 3.7.4 and Java 21. Modules are managed in `build.sbt` using a custom `module` helper defined in `project/BuildSettings.scala`. Local Stockfish integration typically involves the UCI protocol, with some infrastructure already present in `lila.analyse` (ExternalEngine) and `lila.fishnet`.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Scala | 3.7.4 | Language | Current project standard |
| Java | 21 | Runtime | Specified in build.sbt/javacOptions |
| Play Framework | 2.8.18-lila_3.22 | Web Framework | Core of Lila |
| scalachess | 17.14.3 | Chess Logic | Lichess core library for move validation and UCI parsing |

## Architecture Patterns

### Module Inclusion in `build.sbt`
Lila uses a monorepo structure where modules are defined in the `modules/` directory.
To include `analyse` and `tree` in a new module:
```scala
lazy val myNewModule = module("my-new-module",
  Seq(analyse, tree), // Dependencies on other modules
  Seq(...)            // Library dependencies
)
```
Existing definitions:
- `tree`: `lazy val tree = module("tree", Seq(core), Seq(chess.playJson))`
- `analyse`: `lazy val analyse = module("analyse", Seq(tree, memo, ui), tests.bundle)`

### Stockfish / UCI Communication
- **UCI Protocol:** The standard communication is via stdin/stdout.
- **isready check:** Sending `isready` to Stockfish should be followed by waiting for `readyok`.
- **Implementation:** In Scala, this is typically handled via `java.lang.ProcessBuilder` or an Akka Actor wrapper to handle asynchronous stream reading.

## Don't Hand-Roll
| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Move Validation | Custom logic | `scalachess` | Handles variants, legality, and FEN perfectly. |
| UCI Parsing | Regex/String splitting | `scalachess.format.Uci` | Already robustly implemented in the project. |

## Common Pitfalls
- **JVM Version Mismatch:** Ensure the environment uses Java 21 as specified in `javacOptions` and `javaOptions`.
- **Stockfish Pathing:** Stockfish binaries are OS-dependent. The `Dependencies.scala` file already has logic for detecting architecture (`x86_64` vs `aarch_64`).

## Open Questions
- Does `lila.analyse` already contain a local process wrapper for Stockfish, or is it strictly for cloud/external engines? Initial findings point to `ExternalEngine.scala` handling remote/cloud registrations.
- The exact location of the local Stockfish process manager in the Scala side (possibly in `fishnet` or a dedicated sub-module not yet fully explored).
