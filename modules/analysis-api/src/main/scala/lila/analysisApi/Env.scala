package lila.analysisApi

import com.softwaremill.macwire.*
import scala.concurrent.ExecutionContext

final class Env(
    using ec: ExecutionContext
)(
    val annotator: lila.analyse.Annotator
):
  private val stockfishPath = "/usr/games/stockfish"
  
  lazy val orchestrator = new AnalysisOrchestrator(stockfishPath)
  lazy val service = new AnalysisService(orchestrator, annotator)
