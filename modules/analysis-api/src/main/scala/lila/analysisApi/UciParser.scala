package lila.analysisApi

import chess.format.Uci
import chess.eval.{ Eval as Ev, Score }
import lila.tree.Eval

object UciParser:

  def parse(lines: List[String], ply: Int): Option[Eval] =
    // Find the last info line that actually contains a score
    val lastInfo = lines.filter(l => l.startsWith("info ") && l.contains(" score ")).lastOption
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
      
      // Stockfish UCI is relative to the side to move.
      // Lila tree requires all evaluations to be from White's perspective (absolute).
      // After ply 1 (White move), it is Black's turn, so Stockfish gives Black POV. We must invert.
      if (ply % 2 != 0) eval.invert else eval
    }

  private def parseScore(parts: List[String]): Option[Score] =
    val scoreIndex = parts.indexOf("score")
    if (scoreIndex == -1) None
    else
      parts.lift(scoreIndex + 1) match
        case Some("cp") => 
          parts.lift(scoreIndex + 2).flatMap(_.toIntOption).map(Score.cp)
        case Some("mate") => 
          parts.lift(scoreIndex + 2).flatMap(_.toIntOption).map(Score.mate)
        case _ => None
