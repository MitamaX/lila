package lila.tree

import chess.Ply
import chess.eval.Eval as Ev
import chess.eval.WinPercent
import chess.Color

class AdviceConsistencyTest extends munit.FunSuite:

  private def makeInfo(ply: Int, cp: Int) =
    Info(Ply(ply), Eval(Some(Ev.Cp(cp)), None, None), Nil)

  test("100% Consistency: Blunder (Delta <= -0.3)"):
    // White moves (Ply 1), White advantage 100cp -> -250cp
    // WinPercent(100) = 0.1812
    // WinPercent(-250) = -0.4218
    // Delta = -0.4218 - 0.1812 = -0.6030
    val prev = makeInfo(0, 100)
    val info = makeInfo(1, -250)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.Blunder)

  test("100% Consistency: Mistake (Delta <= -0.2)"):
    // White moves (Ply 1), White advantage 100cp -> -50cp
    // WinPercent(100) = 0.1812
    // WinPercent(-50) = -0.0919
    // Delta = -0.0919 - 0.1812 = -0.2731
    val prev = makeInfo(0, 100)
    val info = makeInfo(1, -50)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.Mistake)

  test("100% Consistency: Inaccuracy (Delta <= -0.1)"):
    // White moves (Ply 1), White advantage 100cp -> 20cp
    // WinPercent(100) = 0.1812
    // WinPercent(20) = 0.0368
    // Delta = 0.0368 - 0.1812 = -0.1444
    val prev = makeInfo(0, 100)
    val info = makeInfo(1, 20)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.Inaccuracy)

  test("100% Consistency: Brilliant (Delta >= 0.15)"):
    // White moves (Ply 1), White advantage 0cp -> 100cp
    // WinPercent(0) = 0.0
    // WinPercent(100) = 0.1812
    // Delta = 0.1812 - 0.0 = 0.1812
    val prev = makeInfo(0, 0)
    val info = makeInfo(1, 100)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.Brilliant)

  test("100% Consistency: Great Move (Delta >= 0.05)"):
    // White moves (Ply 1), White advantage 0cp -> 40cp
    // WinPercent(0) = 0.0
    // WinPercent(40) = 0.0735
    // Delta = 0.0735 - 0.0 = 0.0735
    val prev = makeInfo(0, 0)
    val info = makeInfo(1, 40)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.GreatMove)

  test("100% Consistency: Black Move Blunder"):
    // Black moves (Ply 2), White advantage -100cp -> 250cp
    // White WinPercent(-100) = -0.1812
    // White WinPercent(250) = 0.4218
    // Black Gain Delta = WinPercentBefore - WinPercentAfter (since WinPercent is White POV)
    // Delta = -0.1812 - 0.4218 = -0.6030
    val prev = makeInfo(1, -100)
    val info = makeInfo(2, 250)
    val advice = CpAdvice(prev, info)
    assertEquals(advice.get.judgment, Advice.Judgement.Blunder)
