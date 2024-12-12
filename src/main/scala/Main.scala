import streaming._
import zio._

import java.nio.file.Path

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program
      .provide(
        Configuration.live,
        Cleanup.live,
        ReaderImpl.live,
        BufferImpl.live,
        ProducerImpl.live,
        ConsumerImpl.live
      )

  private def pubsub(paths: List[Path]): URIO[Buffer with Consumer with Producer with Configuration, Unit] = for {
    outputPath <- ZIO.serviceWith[Configuration](_.outputPath)
    signal     <- Promise.make[Nothing, Unit]
    producer   <- Producer.produce(paths).ensuring(signal.succeed(())).fork
    consumer   <- Consumer.consume(outputPath, signal).fork
  } yield ()

  private val program = for {
    config <- ZIO.service[Configuration]
    paths  <- Reader.split(config.inputPath)
    _      <- pubsub(paths).awaitAllChildren
    _      <- Cleanup.run
  } yield paths
}
