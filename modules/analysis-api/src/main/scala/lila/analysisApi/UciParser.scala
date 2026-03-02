package lila.analysisApi

import chess.format.Uci
import chess.eval.{ Eval as Ev, Score }
import lila.tree.Eval

object UciParser:

  /**
   * Parses UCI output lines and returns an Eval object.
   * @param lines The output lines from Stockfish (e.g., info lines and bestmove line)
   * @param ply The current move number (1-based, 1 is White's first move)
   */
  def parse(lines: List[String], ply: Int): Option[Eval] =
    val lastInfo = lines.filter(_.startsWith("info ")).lastOption
    val bestMove = lines.find(_.startsWith("bestmove ")).flatMap { line =>
      line.split(" ").lift(1).flatMap(Uci.apply)
    }

    lastInfo.map { info =>
      val parts = info.split(" ").toList
      val score = parseScore(parts)
      val eval = Eval(
        cp = score.flatMap(_.cp),
        mate = score.flatMap(_.mate),
        best = bestMove
      )
      // Lila uses absolute perspective (White is positive).
      // Stockfish UCI uses relative perspective (side to move is positive).
      // If it's Black's turn (ply is even), we need to invert the score.
      if ply % 2 == 0 then eval.invert else eval
    }

  private def parseScore(parts: List[String]): Option[Score] =
    val scoreIndex = parts.indexOf("score")
    if scoreIndex == -1 then None
    else
      parts.lift(scoreIndex + 1) match
        case Some("cp") => 
          parts.lift(scoreIndex + 2).flatMap(_.toIntOption).map(v => Score.Cp(Ev.Cp(v)))
        case Some("mate") => 
          parts.lift(scoreIndex + 2).flatMap(_.toIntOption).map(v => Score.Mate(Ev.Mate(v)))
        case _ => None
