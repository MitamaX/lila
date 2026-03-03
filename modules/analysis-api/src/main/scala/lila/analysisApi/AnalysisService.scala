package lila.analysisApi

import scala.concurrent.{ ExecutionContext, Future }
import lila.tree.{ Analysis, Info, ParseImport }
import lila.analyse.Annotator
import chess.format.pgn.PgnStr
import chess.ErrorStr
import lila.core.id.GameId

final class AnalysisService(
    orchestrator: AnalysisOrchestrator,
    annotator: Annotator
)(using ec: ExecutionContext):

  def analyze(pgn: PgnStr): Future[Either[ErrorStr, AnalysisReport]] =
    ParseImport.full(pgn) match
      case Left(err) => Future.successful(Left(err))
      case Right(importResult) =>
        val moves = importResult.replay.chronoMoves.map(_.toUci.uci)
        orchestrator.analyze(moves, depth = 22).map { infos =>
          val analysis = Analysis(
            id = Analysis.Id(None, "stateless"),
            infos = infos,
            startPly = importResult.replay.setup.ply,
            date = lila.common.nowInstant,
            fk = None,
            nodesPerMove = None
          )
          Right(ReportGenerator(analysis, importResult.parsed.toPgn, annotator))
        }
