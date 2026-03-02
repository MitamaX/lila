package lila.analysisApi

import scala.concurrent.{ ExecutionContext, Future }
import lila.tree.{ Analysis, Info, ParseImport }
import lila.analyse.Annotator
import chess.format.pgn.PgnStr

final class AnalysisService(
    orchestrator: AnalysisOrchestrator,
    annotator: Annotator
)(using ec: ExecutionContext):

  def analyze(pgn: PgnStr): Future[Either[lila.tree.ErrorStr, AnalysisReport]] =
    ParseImport.full(pgn) match
      case Left(err) => Future.successful(Left(err))
      case Right(importResult) =>
        val moves = importResult.replay.chronoMoves.map(_.uci.chars)
        orchestrator.analyze(moves, depth = 12).map { infos =>
          val analysis = Analysis(
            id = Analysis.Id(None, importResult.parsed.tags.gameId | "stateles"),
            infos = infos,
            startPly = importResult.replay.setup.ply,
            date = lila.common.LichessDay.now.dateTime.toInstant,
            fk = None,
            nodesPerMove = None
          )
          Right(ReportGenerator(analysis, importResult.parsed, annotator))
        }
