package lila.analysisApi

import scala.concurrent.{ ExecutionContext, Future }
import java.util.concurrent.Semaphore
import lila.tree.{ Eval, Info }

/**
 * Orchestrates full-game analysis by coordinating multiple StockfishProcesses in parallel.
 */
final class AnalysisOrchestrator(
    path: String
)(using ec: ExecutionContext):

  // Limit concurrent engines to half of available CPU cores to ensure system stability
  private val cores = (Runtime.getRuntime().availableProcessors() / 2).max(1)
  private val semaphore = new Semaphore(cores)

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
        
        // Variation is the best alternative from the previous position
        val variation = prevEval.best.toList.map(u => chess.format.pgn.SanStr(u.uci))
        
        val info = Info(chess.Ply(ply), eval, variation)
        
        // Lichess logic: Only keep the variation (which triggers "X was best") if the move is an Inaccuracy, Mistake, or Blunder.
        // If it's a good move, drop the variation.
        val prevInfo = if (ply == 1) Info.start(chess.Ply(0)) else Info(chess.Ply(ply - 1), prevEval, Nil)
        
        lila.tree.Advice(prevInfo, info) match
          case Some(adv) if adv.judgment.isMistakeOrBlunder || adv.judgment == lila.tree.Advice.Judgement.Inaccuracy => 
            info // Keep variation for mistakes
          case _ => 
            info.dropVariation // Drop variation for Brilliant, Great, or normal moves
      }
    }

  // Returns (ply, Eval)
  private def analyzeSinglePosition(movesSoFar: List[String], ply: Int, depth: Int): Future[(Int, Eval)] = Future {
    semaphore.acquire()
    val engine = new StockfishProcess(path, Nil)
    try
      engine.start()
      engine.send("setoption name Threads value 1") 
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
      engine.stop()
      semaphore.release()
  }
