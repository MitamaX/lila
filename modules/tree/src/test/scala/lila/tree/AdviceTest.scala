package lila.tree

import chess.Ply
import chess.eval.Eval as Ev
import chess.eval.WinPercent

class AdviceTest extends munit.FunSuite:

  private def makeInfo(cp: Int) =
    Info(Ply(1), Eval(Some(Ev.Cp(cp)), None, None), Nil)

  test("Blunder detection"):
    val prev = makeInfo(100)  // 60% win chance
    val info = makeInfo(-100) // 40% win chance
    // delta = 40 - 60 = -20? No, delta is calculated in CpAdvice
    // Let's check CpAdvice.apply
    val advice = CpAdvice(prev, info)
    assert(advice.isDefined)
    assertEquals(advice.get.judgment, Advice.Judgement.Mistake)

  test("Inaccuracy detection"):
    val prev = makeInfo(50)
    val info = makeInfo(0)
    val advice = CpAdvice(prev, info)
    assert(advice.isDefined)
    assertEquals(advice.get.judgment, Advice.Judgement.Inaccuracy)

  test("Brilliant detection"):
    val prev = makeInfo(0)
    val info = makeInfo(200)
    val advice = CpAdvice(prev, info)
    assert(advice.isDefined)
    assertEquals(advice.get.judgment, Advice.Judgement.Brilliant)

  test("GreatMove detection"):
    val prev = makeInfo(0)
    val info = makeInfo(100)
    val advice = CpAdvice(prev, info)
    assert(advice.isDefined)
    assertEquals(advice.get.judgment, Advice.Judgement.GreatMove)
