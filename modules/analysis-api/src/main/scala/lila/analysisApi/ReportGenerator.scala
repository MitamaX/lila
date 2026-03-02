package lila.analysisApi

import play.api.libs.json.*
import lila.tree.{ Analysis, Advice, AccuracyPercent }
import lila.analyse.Annotator

case class AnalysisReport(
    accuracy: Map[chess.Color, Int],
    summary: Map[chess.Color, Map[String, Int]],
    pgn: String,
    evals: List[JsValue]
):
  def asJson: JsValue = Json.obj(
    "accuracy" -> accuracy.map { (c, v) => c.name -> v },
    "summary" -> summary.map { (c, m) => c.name -> m },
    "pgn" -> pgn,
    "evals" -> evals
  )

object ReportGenerator:

  def apply(analysis: Analysis, pgn: chess.format.pgn.Pgn, annotator: Annotator): AnalysisReport =
    val accuracy = Map(
      chess.Color.White -> analysis.accuracy(chess.Color.White).getOrElse(0),
      chess.Color.Black -> analysis.accuracy(chess.Color.Black).getOrElse(0)
    )

    val summary = Map(
      chess.Color.White -> judgementCounts(analysis, chess.Color.White),
      chess.Color.Black -> judgementCounts(analysis, chess.Color.Black)
    )

    // Using dummy game for now as we are stateless
    val game = lila.core.game.Game(
      id = lila.core.game.Game.Id("stateles"),
      whitePlayer = lila.core.game.Player.white,
      blackPlayer = lila.core.game.Player.black,
      binaryMoves = Nil,
      status = chess.Status.Started,
      turns = analysis.infos.size,
      variant = chess.variant.Standard,
      createdAt = lila.common.LichessDay.now.dateTime
    )

    val annotatedPgn = annotator(pgn, game, Some(analysis))

    val evals = analysis.infos.map { info =>
      Json.obj(
        "ply" -> info.ply.value,
        "eval" -> info.eval.cp.map(_.value).getOrElse(0),
        "mate" -> info.eval.mate.map(_.value)
      )
    }

    AnalysisReport(accuracy, summary, annotator.toPgnString(annotatedPgn).value, evals)

  private def judgementCounts(analysis: Analysis, color: chess.Color): Map[String, Int] =
    analysis.advices.filter(_.color == color).foldLeft(Map.empty[String, Int]): (acc, advice) =>
      val name = advice.judgement.name
      acc.updated(name, acc.getOrElse(name, 0) + 1)
