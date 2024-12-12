package streaming

import commons.{Parallelism, StreamOps}
import zio._
import zio.stream._

import java.nio.file.Path
import scala.language.postfixOps

trait Producer {
  def produce(paths: List[Path]): RIO[Buffer, Unit]
}

object Producer {
  def produce(paths: List[Path]): RIO[Buffer with Producer, Unit] =
    ZIO.serviceWithZIO[Producer](_.produce(paths))
}

case class ProducerImpl(buffer: Buffer) extends Producer {
  override def produce(paths: List[Path]): RIO[Buffer, Unit] = StreamOps
    .mergeSortedStreams(paths.map(fileStream))
    .foreach(item => buffer.enqueueAll(List(item)))
    .catchSome { case t: Throwable => ZIO.logError(s"[Producer] Failed while trying to queue: $t") }

  private def fileStream(path: Path): Stream[Throwable, String] =
    ZStream.logInfo(s"[Producer] Started reading ${path.toString} on Thread: ${Parallelism.threadId}") *>
      ZStream
        .fromPath(path)
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        //.map(line => line.toInt)
}

object ProducerImpl {
  val live: URLayer[Buffer, Producer] = ZLayer {
    ZIO.serviceWith[Buffer](ProducerImpl(_))
  }
}
