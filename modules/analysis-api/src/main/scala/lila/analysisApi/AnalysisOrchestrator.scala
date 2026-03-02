package lila.analysisApi

import scala.concurrent.{ ExecutionContext, Future }
import lila.tree.{ Eval, Info }

/**
 * Orchestrates full-game analysis by coordinating StockfishProcess and UciParser.
 */
final class AnalysisOrchestrator(
    process: StockfishProcess
)(using ec: ExecutionContext):

  /**
   * Analyzes a sequence of moves.
   * @param moves List of UCI move strings (e.g., List("e2e4", "e7e5", ...))
   * @param depth The target search depth for each position
   * @return A Future containing a list of Info objects corresponding to each move
   */
  def analyze(moves: List[String], depth: Int): Future[List[Info]] =
    def analyzeRecursive(
        currentMoves: List[String],
        remaining: List[String],
        acc: List[Info]
    ): Future[List[Info]] =
      remaining match
        case Nil => Future.successful(acc.reverse)
        case next :: tail =>
          val allMoves = currentMoves :+ next
          process.position(allMoves)
          process.go(depth).flatMap { lines =>
            val ply = allMoves.size
            val eval = UciParser.parse(lines, ply).getOrElse(lila.tree.evals.empty)
            val info = Info(chess.Ply(ply), eval, Nil)
            analyzeRecursive(allMoves, tail, info :: acc)
          }

    analyzeRecursive(Nil, moves, Nil)
