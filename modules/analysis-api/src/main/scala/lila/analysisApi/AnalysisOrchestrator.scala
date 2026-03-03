package lila.analysisApi

import scala.concurrent.{ ExecutionContext, Future }
import java.util.concurrent.ArrayBlockingQueue
import lila.tree.{ Eval, Info }

/**
 * Orchestrates full-game analysis using a pre-warmed pool of Stockfish engines.
 */
final class AnalysisOrchestrator(
    path: String
)(using ec: ExecutionContext):

  // Unleash full CPU power: Use all available cores except one (for OS/server overhead)
  private val poolSize = (Runtime.getRuntime().availableProcessors() - 1).max(1)
  private val enginePool = new ArrayBlockingQueue[StockfishProcess](poolSize)

  // Warm up the engines on startup
  println(s"Warming up $poolSize Stockfish engines...")
  for (_ <- 1 to poolSize) do
    val engine = new StockfishProcess(path, Nil)
    engine.start()
    engine.send("setoption name Threads value 1") 
    engine.isReady
    enginePool.put(engine)
  println(s"Engine pool initialized with $poolSize engines.")

  def analyze(moves: List[String], depth: Int): Future[List[Info]] =
    println(s"Orchestrating analysis for ${moves.size} moves with depth $depth")
    val allPlies = (0 to moves.size).toList.map(i => moves.take(i))
    
    val futureEvals = Future.traverse(allPlies.zipWithIndex) { case (movesSoFar, ply) =>
      analyzeSinglePosition(movesSoFar, ply, depth)
    }

    futureEvals.map { evalsList =>
      println(s"Analysis complete for all ${evalsList.size} positions")
      val sortedEvals = evalsList.sortBy(_._1)
      
      sortedEvals.tail.map { case (ply, eval) =>
        val prevEval = sortedEvals.find(_._1 == ply - 1).map(_._2).getOrElse(lila.tree.evals.empty)
        
        val playedMoveUci = moves(ply - 1)
        val variation = prevEval.best.toList
          .filter(_.uci != playedMoveUci)
          .map(u => chess.format.pgn.SanStr(u.uci))
          
        val info = Info(chess.Ply(ply), eval, variation)
        
        val prevInfo = if (ply == 1) Info.start(chess.Ply(0)) else Info(chess.Ply(ply - 1), prevEval, Nil)
        
        lila.tree.Advice(prevInfo, info) match
          case Some(adv) if adv.judgment.isMistakeOrBlunder || adv.judgment == lila.tree.Advice.Judgement.Inaccuracy => 
            info
          case _ => 
            info.dropVariation
      }
    }

  private def analyzeSinglePosition(movesSoFar: List[String], ply: Int, depth: Int): Future[(Int, Eval)] = Future {
    // Borrow an engine from the pool (blocks until one is available)
    val engine = enginePool.take()
    try
      // Clear the engine's internal hash table before giving it a new isolated position
      engine.send("ucinewgame")
      engine.isReady

      engine.position(movesSoFar)
      val lines = engine.go(depth)
      
      val eval = UciParser.parse(lines, ply).getOrElse(lila.tree.evals.empty)
      (ply, eval)
    catch
      case e: Exception =>
        println(s"Error evaluating ply $ply: ${e.getMessage}")
        throw e
    finally
      // Return the engine to the pool for the next job
      enginePool.put(engine)
  }

