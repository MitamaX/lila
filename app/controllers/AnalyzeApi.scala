package controllers

import play.api.mvc.*
import scala.concurrent.{ Future, ExecutionContext }
import lila.app.{ *, given }
import lila.analysisApi.AnalysisService
import lila.tree.Analysis

final class AnalyzeApi(
    env: Env,
    service: AnalysisService
)(using ec: ExecutionContext) extends LilaController(env):

  def health = Action:
    Ok("ok").as(TEXT)

  def analyze = Action.async(parse.anyContent): req =>
    val pgnOpt = req.body.asFormUrlEncoded.flatMap(_.get("pgn").flatMap(_.headOption))
      .orElse(req.body.asJson.flatMap(j => (j \ "pgn").asOpt[String]))

    pgnOpt match
      case Some(pgn) =>
        service.analyze(chess.format.pgn.PgnStr(pgn)).flatMap {
          case Right(report) => 
            Future.successful(Ok(report.asJson).as(JSON))
          case Left(err) =>
            Future.successful(BadRequest(err.value).as(TEXT))
        }
      case None =>
        Future.successful(BadRequest("Missing PGN").as(TEXT))
