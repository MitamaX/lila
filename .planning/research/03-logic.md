# Research: Phase 3 - Lichess Logic Integration

## Move Grades (Judgements)

### Current Implementation
- **Inaccuracy (?!)**: winning chance loss >= 10%
- **Mistake (?)**: winning chance loss >= 20%
- **Blunder (??)**: winning chance loss >= 30%

### New Implementation (Lichess Style)
- **Brilliant (!!)**: 
  - A sacrifice is involved (material value of the piece moved or a piece lost in the variation is higher than the piece captured).
  - The move is the best move or at least keeps the position winning/equal.
  - The win percent doesn't drop significantly (delta < 0.05).
- **Great Move (!)**:
  - The only good move in the position (all other moves lead to a significantly worse position).
  - Since we don't have MultiPV data, we might need to approximate this by checking if the evaluation is significantly higher than the previous move's evaluation or if it's a critical "only move" situation.
  - *Correction*: Lichess's "Great Move" is often assigned to the only move that maintains a large advantage.

## Accuracy Score

Lichess uses an exponential model to map Centipawn Loss to Accuracy:
`Accuracy = 103.1668 * exp(-0.0435 * winDiff) - 3.1669 + 1`

This is already implemented in `AccuracyPercent.scala`.

## Implementation Plan

1.  **Modify `Advice.scala`**:
    - Add `Brilliant` and `GreatMove` to `Judgement` enum.
    - Update `Advice.apply` and `CpAdvice.apply` to include logic for these new grades.
    - We need a way to detect sacrifices. This might require looking at the move and the board state (if available) or simply looking at the material change.

2.  **Sacrifice Detection**:
    - Check if the moved piece is lost in the next few plies of the variation.
    - Check if the move is a capture of a lower-valued piece while giving up a higher-valued piece.
    - Since we have `chess` library, we can use its move and board logic.

3.  **Great Move Detection**:
    - If `prev.eval` was significantly worse than `info.eval` (recovery).
    - If it's the only move that maintains the win (difficult to detect without MultiPV).
    - *Alternative*: Define "Great Move" as a move that is the best move and has a high "clutch" value (e.g., in a high-tension position).

4.  **Verification**:
    - Add tests in `AdviceTest.scala` (if it exists) or create one.
    - Verify `Annotator` correctly applies the new glyphs.

## Open Questions
- Does `lila` have access to the full board state during `Advice` calculation?
  - `Info` only has `eval` and `variation`.
  - `Analysis` has a list of `Info`.
  - We might need the `Game` or `Pgn` to get the board state.
