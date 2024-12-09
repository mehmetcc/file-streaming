import streaming.Configuration
import zio._

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = program.provide(Configuration.live)

  private val program = for {
    config <- ZIO.service[Configuration]
    _      <- ZIO.log(config.intermediaryDirectory)
    _      <- ZIO.log(config.chunkSize.toString)
  } yield ()
}
