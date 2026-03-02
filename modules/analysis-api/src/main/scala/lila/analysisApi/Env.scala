package lila.analysisApi

import com.softwaremill.macwire.*
import scala.concurrent.ExecutionContext

final class Env(
    using ec: ExecutionContext
)(
    val annotator: lila.analyse.Annotator
):
  lazy val stockfishProcess = wire[StockfishProcess]
  lazy val orchestrator = wire[AnalysisOrchestrator]
  lazy val service = wire[AnalysisService]
