# Domain Pitfalls

**Domain:** Chess Analysis API (Lila Core Integration)
**Researched:** 2025-03-01
**Overall Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: Evaluation Saturation (WinPercent vs. CPL)
**What goes wrong:** Users report that obvious blunders (e.g., losing a Queen) are not flagged by the API, or minor inaccuracies in equal positions are flagged as blunders.
**Why it happens:** Lichess uses a sigmoid function (`WinPercent`) to map Centipawn (CP) values to winning chances. If a position is already +10.0 (99.9% win) and drops to +5.0 (99.8% win), the delta is < 0.1, so no blunder is recorded. Conversely, a 0.5 CP drop in a 0.0 position might be a 30% win-chance swing, triggering a blunder.
**Consequences:** The tool feels "broken" or "inconsistent" to users accustomed to raw CPL-based analysis.
**Prevention:**
- Use `lila.tree.Advice` logic exactly as-is.
- Educate users via API documentation about "Evaluation Saturation".
- Provide both `AccuracyPercent` and `AverageCentipawnLoss` (ACPL) to give a fuller picture.

### Pitfall 2: Dependency Chain Explosion
**What goes wrong:** Attempting to use `Advice.scala` as a standalone file leads to hundreds of compilation errors.
**Why it happens:** Lila is a modular but highly interdependent monolith. `Advice` depends on `Info`, `Eval`, `Score`, `WinPercent`, `SanStr`, `Glyph`, and `Color`. These in turn depend on `scalachess`, `scalalib`, and various `lila.common` utilities.
**Consequences:** Massive "dependency bloat" or a stalled project due to the complexity of decoupling.
**Prevention:**
- Instead of cherry-picking files, include `modules/tree` and `modules/common` as project dependencies.
- Use a "Lichess-lite" approach: Keep the core Scala logic but mock or replace heavy Play Framework/Database dependencies.
- Use `sbt` subprojects to mirror Lila's structure for easier updates.

### Pitfall 3: Engine Depth and Version Inconsistency
**What goes wrong:** The API returns different "Best Moves" or "Accuracies" than the Lichess website for the same game.
**Why it happens:** Lichess analysis results depend heavily on the engine version (Stockfish 16+ NNUE) and the search depth/nodes. Small variations in evaluation (e.g., +0.1 vs +0.3) can cross the threshold for Inaccuracy/Mistake.
**Consequences:** Loss of the "100% Lichess compatibility" value proposition.
**Prevention:**
- Standardize on the exact Stockfish version used by Lichess (currently SF 16.1).
- Set a fixed analysis depth (e.g., Depth 22 or 2.5 million nodes) that matches Lichess server-side analysis.
- Ensure NNUE is enabled and the correct `.nnue` file is loaded.

## Moderate Pitfalls

### Pitfall 4: Accuracy Formula Complexity
**What goes wrong:** The calculated game accuracy (e.g., 85%) doesn't match Lichess (e.g., 92%).
**Why it happens:** Lichess `gameAccuracy` is not a simple average. It uses a **volatility-weighted mean** combined with a **harmonic mean** of move-by-move accuracies. The harmonic mean specifically punishes blunders more severely.
**Prevention:**
- Do not implement a custom averaging logic.
- Extract and use `AccuracyPercent.gameAccuracy` from `modules/analyse/src/main/AccuracyPercent.scala`.
- Port or depend on `scalalib.Maths` for the weighted and harmonic mean implementations.

### Pitfall 5: Multi-PV Performance Bottleneck
**What goes wrong:** The API is extremely slow, taking minutes to analyze a single game.
**Why it happens:** To find "Advice" (blunder/mistake), you need to know the evaluation of the *played move* and the *best move*. If the played move wasn't the best, you must run a separate engine search for it, or use Multi-PV. Multi-PV 2+ is significantly slower than Single-PV.
**Prevention:**
- Use a pool of Stockfish processes with limited CPU cores.
- Implement an asynchronous analysis pipeline (Submit PGN -> Get Job ID -> Webhook/Polling).
- Optimize by only searching for the "played move" if it differs from the "best move" found in a high-depth Single-PV search.

## Minor Pitfalls

### Pitfall 6: Mate Sequence Logic
**What goes wrong:** Users complain about "Inaccuracies" when they play a "Mate in 5" instead of a "Mate in 3".
**Why it happens:** Lichess logic (in `MateAdvice.scala`) flags "MateDelayed" or "MateCreated" transitions. Losing a forced mate or delaying it is mathematically an inaccuracy.
**Prevention:** Preserve this behavior for 100% compatibility, but consider adding a toggle to "ignore mate-to-mate inaccuracies" for a more "human" feel.

### Pitfall 7: PGN Comment Merging
**What goes wrong:** Generated PGNs have messy or duplicate comments (e.g., `[%eval 0.1] [%eval 0.2]`).
**Why it happens:** Games often come with existing comments (clock times, human notes). Simply appending engine evaluations can break PGN parsers.
**Prevention:**
- Use `lila.analyse.Annotator` to merge comments properly.
- Use `[%eval ...]` and `[%clk ...]` tags correctly within the `{}` braces.

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Core Setup | Dependency Chain Explosion | Use `sbt` subprojects; include `modules/tree`. |
| Engine Integration | Depth/Version Inconsistency | Hardcode SF 16+ and Depth 22. |
| Logic Implementation | WinPercent Saturation | Implement sigmoid-based `WinPercent` early. |
| API Development | Long-Running Requests | Use async tasks + Job IDs, not sync responses. |
| Accuracy Calculation | Formula Complexity | Direct port of `AccuracyPercent.scala`. |

## Sources

- [Lichess Source: Advice.scala](https://github.com/lichess-org/lila/blob/master/modules/tree/src/main/Advice.scala)
- [Lichess Source: AccuracyPercent.scala](https://github.com/lichess-org/lila/blob/master/modules/analyse/src/main/AccuracyPercent.scala)
- [Lichess Blog: Accuracy Metrics](https://lichess.org/blog/W6queR8AAOfP79s-p/accuracy-metrics)
- [Community Discussion: Why is this not a blunder?](https://lichess.org/forum/general-chess-discussion/why-is-this-not-a-blunder)
