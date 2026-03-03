package lila.analysisApi

import play.api.libs.json.*
import lila.tree.{ Analysis, Advice }
import lila.analyse.{ Annotator, AccuracyPercent }
import lila.core.game.{ Game, Player, GameMetadata }
import lila.core.id.{ GameId, GamePlayerId }
import chess.{ ByColor, Color, Status, Game as ChessGame }

case class AnalysisReport(
    accuracy: Map[Color, Int],
    summary: Map[Color, Map[String, Int]],
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
    val accuracies = AccuracyPercent.gameAccuracy(Color.White, analysis)
    
    // Debug print (will show in docker logs)
    println(s"DEBUG - Accuracies: $accuracies")

    val accuracy = Map(
      Color.White -> accuracies.map(_.white.value.toInt).getOrElse(0),
      Color.Black -> accuracies.map(_.black.value.toInt).getOrElse(0)
    )

    val summary = Map(
      Color.White -> judgmentCounts(analysis, Color.White),
      Color.Black -> judgmentCounts(analysis, Color.Black)
    )

    // Using dummy game for now as we are stateless
    val game = Game(
      id = GameId("stateless"),
      players = ByColor(
        Player(
          id = GamePlayerId("p0"), 
          color = Color.White,
          aiLevel = None,
          isWinner = None,
          isOfferingDraw = false,
          proposeTakebackAt = chess.Ply.initial,
          userId = None,
          rating = None,
          ratingDiff = None,
          provisional = chess.rating.RatingProvisional(false),
          blurs = lila.core.game.Blurs(0),
          berserk = false,
          blindfold = false,
          name = None
        ),
        Player(
          id = GamePlayerId("p1"), 
          color = Color.Black,
          aiLevel = None,
          isWinner = None,
          isOfferingDraw = false,
          proposeTakebackAt = chess.Ply.initial,
          userId = None,
          rating = None,
          ratingDiff = None,
          provisional = chess.rating.RatingProvisional(false),
          blurs = lila.core.game.Blurs(0),
          berserk = false,
          blindfold = false,
          name = None
        )
      ),
      chess = ChessGame(chess.variant.Standard),
      status = Status.Started,
      daysPerTurn = None,
      metadata = GameMetadata(
        source = Some(lila.core.game.Source.Api),
        pgnImport = None,
        tournamentId = None,
        swissId = None,
        simulId = None,
        analysed = true,
        drawOffers = lila.core.game.GameDrawOffers(white = Set.empty, black = Set.empty),
        rules = Set.empty
      ),
      createdAt = lila.common.nowInstant,
      movedAt = lila.common.nowInstant
    )

    val annotatedPgn = annotator.addEvals(pgn, analysis)

    val advicesByPly = analysis.advices.map(a => a.ply -> a).toMap

    val evals = analysis.infos.map { info =>
      val baseObj = Json.obj(
        "ply" -> info.ply.value,
        "eval" -> info.eval.score.flatMap(_.cp).map(_.value).getOrElse(0),
        "mate" -> info.eval.mate.map(_.value)
      )
      
      advicesByPly.get(info.ply) match
        case Some(advice) => 
          baseObj ++ Json.obj(
            "judgment" -> Json.obj(
              "name" -> advice.judgment.name,
              "comment" -> advice.makeComment(withEval = false).value
            )
          )
        case None => baseObj
    }

    AnalysisReport(accuracy, summary, annotator.toPgnString(annotatedPgn).value, evals)

  private def judgmentCounts(analysis: Analysis, color: Color): Map[String, Int] =
    analysis.advices.filter(_.color == color).foldLeft(Map.empty[String, Int]): (acc, advice) =>
      val name = advice.judgment.name
      acc.updated(name, acc.getOrElse(name, 0) + 1)
