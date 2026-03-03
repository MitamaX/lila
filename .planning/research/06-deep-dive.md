# Phase 6: Lichess Deep Dive - Research

**Researched:** 2026-03-03
**Domain:** Chess Analysis & Move Judgement
**Confidence:** HIGH

## Summary
Investigation of the Lichess source code (`lila` and `scalachess`) reveals that move judgements (Inaccuracy, Mistake, Blunder, Brilliant, Great Move) are not based on linear Centipawn (CP) loss. Instead, they are calculated based on the **delta of winning chances**. This model better reflects the practical difficulty and impact of a move on the game's outcome.

## Standard Stack
*   **scalachess**: Handles move generation, PGN parsing, and defines `Glyph.MoveAssessment`.
*   **lila-tree**: Implements `Advice.scala`, the core logic for translating engine evaluations into human-readable judgements.

## Architecture Patterns: Move Judgement Logic
Lichess uses the `Advice` trait to wrap evaluation deltas.

### CP-Based Judgements
The `CpAdvice` object calculates the winning probability before and after a move:
```scala
val prevWinningChances = WinPercent.winningChances(cp)
val currentWinningChances = WinPercent.winningChances(infoCp)
val delta = (currentWinningChances - prevWinningChances)
```
Thresholds:
- `<= -0.3` -> Blunder
- `<= -0.2` -> Mistake
- `<= -0.1` -> Inaccuracy
- `>= 0.15` -> Brilliant
- `>= 0.05` -> Great Move

### Mate-Based Judgements
- **MateCreated**: If a move makes a previously non-forced mate unavoidable.
- **MateLost**: If a move loses a forced mate sequence.
- **MateDelayed**: If a mate is still forced but takes longer (often ignored for judgement unless significant).

## Don't Hand-Roll
*   **Winning Chance Formula**: Do not use a generic sigmoid. Use the exact coefficients from `WinPercent.scala` to ensure accuracy matches Lichess.
*   **Accuracy %**: Use `AccuracyPercent.scala` logic which aggregates these winning chance deltas over the whole game.

## Common Pitfalls
1.  **Linear CP Loss**: Using `loss > 300` for blunders will fail to match Lichess in high-eval or endgame positions.
2.  **Stockfish Versions**: Evaluation can vary between Stockfish versions; Lichess currently targets SF 16+ NNUE.

## Strategy for Integration
1.  **Port `WinPercent`**: Implement the `winningChances` function exactly as found in the Lichess codebase.
2.  **Standardize `Advice`**: Ensure our analysis engine emits `CpAdvice` and `MateAdvice` objects following the Lichess thresholds.
3.  **Test against Official API**: Use games with known Lichess analysis results as a validation suite.
