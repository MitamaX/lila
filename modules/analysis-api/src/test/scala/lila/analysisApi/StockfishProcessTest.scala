package lila.analysisApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.Await

class StockfishProcessTest extends munit.FunSuite:

  val stockfishPath = sys.env.getOrElse("STOCKFISH_PATH", "stockfish")

  test("isready check") {
    // skip if stockfish is not installed/configured for testing
    val process = new StockfishProcess(stockfishPath)
    try {
      process.start()
      val ready = Await.result(process.isReady, 5.seconds)
      assert(ready)
    } catch {
      case _: java.io.IOException => println("Stockfish binary not found, skipping test")
    } finally {
      process.stop()
    }
  }
