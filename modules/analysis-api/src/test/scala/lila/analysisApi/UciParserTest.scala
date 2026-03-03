package lila.analysisApi

import chess.format.Uci
import chess.eval.{ Eval as Ev }

class UciParserTest extends munit.FunSuite:

  test("parse simple cp score for White (ply 1)") {
    val lines = List(
      "info depth 20 seldepth 30 multipv 1 score cp 15 nodes 1000 nps 100000 hashfull 0 tbhits 0 time 10 pv e2e4",
      "bestmove e2e4 ponder e7e5"
    )
    val eval = UciParser.parse(lines, 1).get
    assertEquals(eval.cp, Some(Ev.Cp(15)))
    assertEquals(eval.mate, None)
    assertEquals(eval.best, Some(Uci("e2e4").get))
  }

  test("parse simple cp score for Black (ply 2) - should invert") {
    val lines = List(
      "info depth 20 score cp 15 pv e7e5",
      "bestmove e7e5"
    )
    val eval = UciParser.parse(lines, 2).get
    // Stockfish says +15 for Black (the side to move).
    // Lila wants absolute perspective, so White is -15.
    assertEquals(eval.cp, Some(Ev.Cp(-15))) 
    assertEquals(eval.best, Some(Uci("e7e5").get))
  }

  test("parse mate score") {
    val lines = List(
      "info depth 20 score mate 5 pv d1h5",
      "bestmove d1h5"
    )
    val eval = UciParser.parse(lines, 1).get
    assertEquals(eval.mate, Some(Ev.Mate(5)))
  }

  test("handle missing score") {
    val lines = List(
      "info depth 20 nodes 1000",
      "bestmove e2e4"
    )
    val eval = UciParser.parse(lines, 1).get
    assertEquals(eval.cp, None)
    assertEquals(eval.mate, None)
    assertEquals(eval.best, Some(Uci("e2e4").get))
  }
