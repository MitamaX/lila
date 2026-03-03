package lila.analyse

import chess.eval.WinPercent
import lila.tree.Eval
import chess.Color

class AccuracyPercentTest extends munit.FunSuite:

  test("Accuracy from win percents"):
    val before = WinPercent.fromCentiPawns(chess.eval.Eval.Cp(100))
    val after = WinPercent.fromCentiPawns(chess.eval.Eval.Cp(100))
    val accuracy = AccuracyPercent.fromWinPercents(before, after)
    assertEquals(accuracy.toInt, 100)

  test("Accuracy with loss"):
    val before = WinPercent.fromCentiPawns(chess.eval.Eval.Cp(200))
    val after = WinPercent.fromCentiPawns(chess.eval.Eval.Cp(0))
    val accuracy = AccuracyPercent.fromWinPercents(before, after)
    assert(accuracy.toInt < 50)
    assert(accuracy.toInt > 0)
