package streaming

import commons.{FileOps, Parallelism}
import zio.stream.ZStream
import zio.{Promise, RIO, Schedule, Task, UIO, URLayer, ZIO, ZLayer, durationInt}

import scala.language.postfixOps

trait Consumer {
  def consume(outputPath: String, signal: Promise[Nothing, Unit]): RIO[Buffer, Unit]
}

object Consumer {
  def consume(outputPath: String, signal: Promise[Nothing, Unit]): RIO[Buffer with Consumer, Unit] =
    ZIO.serviceWithZIO[Consumer](_.consume(outputPath, signal))
}

case class ConsumerImpl(buffer: Buffer) extends Consumer {
  override def consume(outputPath: String, signal: Promise[Nothing, Unit]): RIO[Buffer, Unit] =
    ZStream
      .fromSchedule(Schedule.fixed(1 second))
      .mapZIO(_ => flush(outputPath))
      .takeUntilZIO(_ => isFinished(signal))
      .runDrain

  private def isFinished(signal: Promise[Nothing, Unit]): UIO[Boolean] = for {
    producerDone <- signal.isDone
    emptyBuffer  <- buffer.dequeueAll.map(_.isEmpty)
  } yield producerDone && emptyBuffer

  private def flush(outputPath: String): Task[Unit] = for {
    items <- buffer.dequeueAll
    sorted = items.sorted
    _ <- ZIO.when(sorted.nonEmpty) {
           ZIO.logDebug(s"[Consumer] Dequeuing ${sorted.length} items on Thread: ${Parallelism.threadId}") *>
             FileOps
               .append(outputPath, sorted.toArray.map(_.toInt))
               .tapBoth(
                 err => ZIO.logError(s"[Flush] Failed to write: ${err.getMessage}"),
                 _ =>
                   ZIO.logInfo(
                     s"[Flush] Successfully flushed ${sorted.length} items on Thread: ${Parallelism.threadId}"
                   )
               )
         }
  } yield ()
}

object ConsumerImpl {
  val live: URLayer[Buffer, Consumer] = ZLayer {
    ZIO.serviceWith[Buffer](ConsumerImpl(_))
  }
}
