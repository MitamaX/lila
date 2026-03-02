package controllers

import play.api.mvc.*
import scala.concurrent.Future
import lila.app.{ *, given }
import lila.analysisApi.AnalysisService
import lila.tree.Analysis

final class AnalyzeApi(
    env: Env,
    service: AnalysisService
) extends LilaController(env):

  def analyze = OpenBody: req =>
    req.body.asFormUrlEncoded.flatMap(_.get("pgn").flatMap(_.headOption)) match
      case Some(pgn) =>
        service.analyze(chess.format.pgn.PgnStr(pgn)).flatMap {
          case Right(report) => 
            Fu(Ok(report.asJson).as(JSON))
          case Left(err) =>
            Fu(BadRequest(err.value).as(TEXT))
        }
      case None =>
        Fu(BadRequest("Missing PGN").as(TEXT))
