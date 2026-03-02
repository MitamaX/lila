package lila.analysisApi

import java.io.{ BufferedReader, InputStreamReader, PrintWriter }
import scala.concurrent.{ ExecutionContext, Future }
import lila.common.ThreadLocalRandom

final class StockfishProcess(
    path: String,
    args: List[String] = Nil
)(using ec: ExecutionContext):

  private var process: Option[Process] = None
  private var out: Option[PrintWriter] = None
  private var in: Option[BufferedReader] = None

  def start(): Unit =
    val pb = new ProcessBuilder((path :: args)*)
    val p = pb.start()
    process = Some(p)
    out = Some(new PrintWriter(p.getOutputStream, true))
    in = Some(new BufferedReader(new InputStreamReader(p.getInputStream)))

  def send(command: String): Unit =
    out.foreach(_.println(command))

  def readUntil(predicate: String => Boolean): Future[List[String]] = Future:
    var lines = List.empty[String]
    var found = false
    while !found && in.isDefined do
      val line = in.get.readLine()
      if line == null then found = true
      else
        lines = line :: lines
        if predicate(line) then found = true
    lines.reverse

  def isReady: Future[Boolean] =
    send("isready")
    readUntil(_ == "readyok").map(_.contains("readyok"))

  def position(moves: List[String]): Unit =
    if moves.isEmpty then send("position startpos")
    else send(s"position startpos moves ${moves.mkString(" ")}")

  def go(depth: Int): Future[List[String]] =
    send(s"go depth $depth")
    readUntil(_.startsWith("bestmove"))

  def stop(): Unit =
    send("quit")
    Thread.sleep(100)
    process.foreach(_.destroy())
    process = None
    out = None
    in = None
