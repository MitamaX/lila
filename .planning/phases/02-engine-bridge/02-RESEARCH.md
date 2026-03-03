# Phase 2: Engine Bridge - Research

**Researched:** 2026-03-02
**Domain:** Stockfish UCI Orchestration & Lila Model Mapping
**Confidence:** HIGH

## Summary

Phase 2 focuses on the "bridge" between raw Stockfish UCI output and the internal Lila domain models (`lila.tree.Eval`, `lila.tree.Info`). Research confirms that while Lichess typically offloads analysis to Fishnet (distributed workers), the core logic for constructing an `Analysis` object from evaluations resides in `modules/fishnet` (via `AnalysisBuilder`) and `modules/tree`. 

To implement a standalone Engine Bridge, we must build a local orchestrator that manages Stockfish process lifecycles, parses UCI `info` lines, and maps them to the sigmoid-ready `Eval` structures used by Lichess. The primary challenge is not just running the engine, but ensuring the evaluations are correctly inverted based on player turn to match `lila.tree.Info` expectations.

**Primary recommendation:** Use Akka Actors/Streams to manage Stockfish process I/O and leverage the `lila.tree.Eval` and `lila.tree.Info` case classes directly for data mapping to ensure 100% compatibility with Phase 3 logic.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Lichess 모듈(`modules/analyse`, `modules/tree`)을 직접 참조하여 로직의 동일성을 보장함.
- Stockfish 16+ NNUE를 기본 엔진으로 채택함.

### Claude's Discretion
- 엔진 오케스트레이션 방식 (Akka Actors vs Streams).
- 병렬 분석 처리 전략.

### Deferred Ideas (OUT OF SCOPE)
- Fishnet(분산 분석) 연동 지원 (v2로 연기됨).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| ENG-02 | 게임 전체 수에 대한 순차적/병렬 엔진 분석 수행 및 평가값(CP/Mate) 추출 | Akka-based process management is the standard pattern in the Scala ecosystem for long-running CLI tools. |
| ENG-03 | 엔진 분석 데이터를 Lichess `Eval` 모델로 매핑 | `lila.tree.Eval` and `lila.tree.Info` identified in `modules/tree/src/main/eval.scala`. |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Stockfish | 16.1+ | Chess Engine | Lichess production standard for accuracy calculation. |
| Akka Streams | 2.6.21 | Engine I/O | Handles asynchronous byte streams from process stdout/stdin safely. |
| lila.tree | - | Domain Models | Contains `Eval`, `Info`, and `Analysis` types required for Phase 3. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| scala-sys-process | Standard | Process Spawn | Low-level process launching. |

## Architecture Patterns

### Recommended Project Structure
```
modules/engine-bridge/
├── src/main/scala/lila/engine/
│   ├── UciParser.scala      # Maps UCI strings to lila.tree.Eval
│   ├── EngineProcess.scala  # Lifecycle management of SF binary
│   └── AnalysisStream.scala # Orchestrates the PGN evaluation flow
```

### Pattern 1: Sigmoid-Ready Evaluation Mapping
**What:** Mapping UCI `cp` and `mate` to `lila.tree.Eval`.
**When to use:** Every move evaluation.
**Example:**
```scala
// Source: modules/tree/src/main/eval.scala
case class Eval(cp: Option[Ev.Cp], mate: Option[Ev.Mate], best: Option[Uci]):
  def invert = copy(cp = cp.map(_.invert), mate = mate.map(_.invert))

// Critical: In Lila, Info objects for Black moves must be inverted
// to keep evaluations relative to the side to move or white-perspective 
// depending on specific usage in AccuracyPercent.
```

### Anti-Patterns to Avoid
- **Blocking I/O:** Reading from the engine stdout in a blocking loop will freeze the API. Use Akka Streams or dedicated execution contexts.
- **Manual String Concatenation for UCI:** Use structured UCI commands to avoid command injection or parsing errors.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| UCI Parsing | Custom Regex | `chess.format.Uci` | Handles all edge cases of move notation and engine-specific quirks. |
| Move Navigation | List[Move] | `chess.Position` | Correctly handles FEN generation and move validation between steps. |

## Common Pitfalls

### Pitfall 1: Evaluation Perspective (White vs. Side-to-Move)
**What goes wrong:** Stockfish provides evaluations relative to the side-to-move, but Lichess `Info` objects often expect a specific perspective for calculations.
**How to avoid:** Explicitly use `info.invert` for black plies when constructing the `Analysis` list.

### Pitfall 2: Engine Saturation/Throttling
**What goes wrong:** Running multiple parallel analyses can saturate CPU, leading to engine timeouts or "isready" failures.
**How to avoid:** Implement a `Semaphore` or Akka Actor pool to limit concurrent Stockfish instances based on available CPU cores.

## Code Examples

### Mapping UCI Info to Lila Info
```scala
// Example inspired by lila/modules/fishnet/src/main/AnalysisBuilder.scala
def makeInfo(ply: Ply, score: Score, bestMove: Uci): Info = {
  val eval = Eval(score.cp, score.mate, Some(bestMove))
  val info = Info(ply, eval, Nil)
  if (ply.isOdd) info.invert else info // Logic depends on who played the move
}
```

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | MUnit / ScalaTest |
| Quick run command | `sbt "project engineBridge; test"` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command |
|--------|----------|-----------|-------------------|
| ENG-02 | Sequential analysis of 10 moves | Integration | `sbt "testOnly *EngineBridgeSpec -- -z sequential"` |
| ENG-03 | UCI "cp 15" maps to Eval(Some(15), None) | Unit | `sbt "testOnly *UciParserSpec"` |

## Sources

### Primary (HIGH confidence)
- `lila/modules/tree/src/main/eval.scala` - Core `Eval` model definition.
- `lila/modules/tree/src/main/Info.scala` - `Info` encoding/decoding and perspective logic.
- `lila/modules/fishnet/src/main/AnalysisBuilder.scala` - Real-world example of building `Analysis` objects from engine results.

## Metadata
**Confidence breakdown:**
- Standard stack: HIGH
- Architecture: HIGH
- Pitfalls: MEDIUM (requires careful implementation of perspective)

**Research date:** 2026-03-02
**Valid until:** 2026-04-01
