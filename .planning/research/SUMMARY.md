# Project Research Summary

**Project:** Lila Analysis API
**Domain:** Chess Analysis API (Lichess Core Integration)
**Researched:** 2026-03-01
**Confidence:** HIGH

## Executive Summary

The Lila Analysis API is a specialized service designed to provide 100% Lichess-compatible chess analysis by directly integrating with the existing `lila` modular codebase (specifically `modules/analyse`, `modules/game`, and `modules/tree`). Unlike general-purpose chess APIs, this project aims to replicate Lichess’s unique accuracy models (`AccuracyPercent`) and move judgments (`Advice`), ensuring consistency with the Lichess web platform. The core challenge lies in managing the complex dependency chain of the Scala-based Lichess monolith while maintaining a portable, high-performance API.

The recommended approach is to build a stateless microservice using **Scala 3**, **Akka HTTP**, and **Tapir**, matching the Lichess tech stack to ensure binary compatibility. By treating the analysis logic as a "Stateless Logic Injection" pattern, the API avoids heavy database dependencies (MongoDB) while leveraging the battle-tested Stockfish 16+ engine. Key risks include "Evaluation Saturation" where sigmoid-based win-probability models differ from raw centipawn loss, and "Dependency Chain Explosion" when attempting to decouple Lichess modules. These will be mitigated by strictly following the Lichess module structure and hardcoding engine parameters (Depth 22, SF 16.1) to match production server behavior.

## Key Findings

### Recommended Stack

The stack is strictly aligned with the Lichess ecosystem to ensure seamless integration and binary compatibility. It uses the established Akka/Scala patterns found in the `lila` codebase.

**Core technologies:**
- **Scala 3.3.5 (LTS)**: Language — Current Lichess standard; provides type-safety for chess domain modeling.
- **Akka HTTP 10.2.10**: Web Server — High-performance and matches the Akka 2.6 ecosystem used in Lichess.
- **Tapir 1.10.15**: API DSL — Declarative, type-safe API definitions with automatic OpenAPI/Swagger generation.
- **Akka 2.6.21**: Runtime — Exact version used in Lichess modules to ensure compatibility.
- **Stockfish 16.1+**: Engine — Industry standard for chess analysis, matching Lichess server-side evaluations.
- **Play-JSON 3.0.6**: Serialization — Mandatory for compatibility with Lichess data formats.

### Expected Features

*Note: Inferred from PROJECT.md as FEATURES.md was unavailable.*

**Must have (table stakes):**
- **PGN Analysis**: Basic UCI Stockfish integration to extract CP/Mate evaluations.
- **Lichess Accuracy**: Implementation of `AccuracyPercent` (Win-probability based).
- **Lichess Judgments**: Identification of Blunders, Mistakes, and Inaccuracies via `Advice` logic.
- **Annotated PGN**: Generation of Lichess-style PGNs with engine comments and symbols.

**Should have (competitive):**
- **Multi-PV Support**: Better analysis by evaluating multiple candidate moves.
- **In-memory Caching**: Caching evaluations for common positions to improve performance.
- **Async Job Management**: Handling long-running analysis tasks without blocking API requests.

**Defer (v2+):**
- **Distributed Analysis**: Fishnet-style worker nodes for massive scale.
- **Persistence Layer**: Database storage for analysis history.

### Architecture Approach

The architecture follows a **Stateless Service Layer** pattern within the `lila` multi-module project. It decouples the core logic from the Lichess database, allowing the API to operate as a portable microservice.

**Major components:**
1. **`analysis-api` Module**: Entry point handling PGN parsing and request orchestration using Tapir/Akka-HTTP.
2. **`EngineRunner`**: Manages local Stockfish process pools and maps UCI output to `lila.tree` models.
3. **`lila.analyse` Logic**: Direct usage of `AccuracyPercent` and `Annotator` for high-level metrics.
4. **`lila.tree` Models**: Core data structures (`Analysis`, `Advice`, `Info`) shared across Lichess.

### Critical Pitfalls

1. **Evaluation Saturation** — Lichess uses a sigmoid function (`WinPercent`) for judgments. Differences between CP and Win% can make the tool feel "broken" if not properly explained or implemented.
2. **Dependency Chain Explosion** — Lila modules are highly interdependent. Avoid cherry-picking files; instead, include `modules/tree` and `modules/common` as project dependencies.
3. **Engine Depth/Version Mismatch** — Using a different SF version or depth than Lichess (Depth 22) will lead to inconsistent results.
4. **Accuracy Formula Complexity** — `gameAccuracy` uses a volatility-weighted harmonic mean, not a simple average. Port `AccuracyPercent.scala` directly.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Foundation & Dependency Setup
**Rationale:** Establishing the build environment is critical due to the complex dependency chain of Lichess.
**Delivers:** SBT multi-module project with verified access to `lila.common`, `lila.tree`, and `lila.game`.
**Addresses:** CORE-01 (Lichess core integration).
**Avoids:** Pitfall 2 (Dependency Chain Explosion).

### Phase 2: Engine Orchestration Layer
**Rationale:** Establishing the raw evaluation source is a prerequisite for any analysis logic.
**Delivers:** `EngineRunner` capable of managing Stockfish 16.1+ processes and producing UCI evaluation streams.
**Uses:** Stockfish 16.1, Akka Streams.
**Implements:** `EngineRunner` component.

### Phase 3: Lichess Analysis Logic Integration
**Rationale:** The core value proposition depends on the parity of `Accuracy` and `Advice` logic.
**Delivers:** Logic to transform raw evaluations into `lila.tree.Analysis` and calculate `AccuracyPercent`.
**Addresses:** REPORT-01 (Accuracy), REPORT-02 (Judgments).
**Avoids:** Pitfall 4 (Accuracy Formula Complexity).

### Phase 4: API & Annotation Layer
**Rationale:** Exposing the logic and providing user-friendly output is the final step for a usable service.
**Delivers:** REST API endpoints and annotated PGN generation via `Annotator`.
**Addresses:** API-01 (API Endpoints), ANNOT-01 (Annotated PGN).
**Avoids:** Pitfall 7 (PGN Comment Merging).

### Phase Ordering Rationale

- **Dependency-First**: We cannot build the logic without first solving the Scala/SBT dependency mapping for `lila` modules.
- **Engine-Before-Logic**: Analysis logic (`Advice`) depends on having a stream of `Info` objects from the engine.
- **Stateless Focus**: By prioritizing the stateless logic first, we ensure the core value is delivered before considering any potential persistence needs.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1 (Foundation):** Needs research into exact SBT subproject configuration to include `lila` modules without pulling in unnecessary Play Framework components.
- **Phase 2 (Engine):** Needs validation of Stockfish process management within Akka (resource isolation/CPU pinning).

Phases with standard patterns (skip research-phase):
- **Phase 4 (API):** Tapir and Akka HTTP patterns for REST APIs are well-documented and standard in the Scala ecosystem.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Aligned with Lichess `Dependencies.scala` and `build.sbt`. |
| Features | MEDIUM | Inferred from `PROJECT.md`; `FEATURES.md` was missing in research phase. |
| Architecture | HIGH | Directly analyzed from `lila` module structure and `AccuracyPercent.scala`. |
| Pitfalls | HIGH | Deeply understood constraints of Lichess evaluation models and dependencies. |

**Overall confidence:** HIGH

### Gaps to Address

- **Missing FEATURES.md**: The specific feature set was inferred from `PROJECT.md`. Needs validation of specific requirements like Multi-PV or Caching.
- **Build-time Binary Compatibility**: Verification that `lila` modules can be compiled with Scala 3.3.5 without breaking existing Scala 2.13 code (using `for3Use2_13`).

## Sources

### Primary (HIGH confidence)
- `modules/analyse/src/main/AccuracyPercent.scala` — Lichess accuracy model.
- `modules/tree/src/main/Advice.scala` — Lichess blunder/mistake logic.
- `project/Dependencies.scala` — Lichess library versions (Akka 2.6.21, Scala 3.3.x).

### Secondary (MEDIUM confidence)
- [Lichess Blog: Accuracy Metrics](https://lichess.org/blog/W6queR8AAOfP79s-p/accuracy-metrics) — Conceptual overview.
- [Official Tapir Docs](https://tapir.softwaremill.com/) — API implementation patterns.

---
*Research completed: 2026-03-01*
*Ready for roadmap: yes*
