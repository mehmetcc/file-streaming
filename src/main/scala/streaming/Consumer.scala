package streaming

import commons.{FileOps, Parallelism}
import zio.stream.ZStream
import zio.{RIO, Schedule, Task, URLayer, ZIO, ZLayer, durationInt}

import scala.language.postfixOps

trait Consumer {
  def consume(outputPath: String): RIO[Buffer, Unit]
}

object Consumer {
  def consume(outputPath: String): RIO[Buffer with Consumer, Unit] =
    ZIO.serviceWithZIO[Consumer](_.consume(outputPath))
}

case class ConsumerImpl(buffer: Buffer) extends Consumer {
  override def consume(outputPath: String): RIO[Buffer, Unit] =
    ZStream
      .fromSchedule(Schedule.fixed(1 second))
      .mapZIO(_ => flush(outputPath))
      .runDrain

  private def flush(outputPath: String): Task[Unit] = for {
    items <- buffer.dequeueAll
    sorted = items.sorted
    _ <- ZIO.when(sorted.nonEmpty) {
           ZIO.logDebug(s"[Consumer] Dequeuing ${sorted.length} items on Thread: ${Parallelism.threadId}") *>
             FileOps
               .append(outputPath, sorted.toArray)
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
