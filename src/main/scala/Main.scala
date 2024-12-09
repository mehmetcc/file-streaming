import streaming.{Configuration, Reader, ReaderImpl}
import zio._

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program.provide(Configuration.live, ReaderImpl.live)

  private val program = for {
    config <- ZIO.service[Configuration]
    paths  <- Reader.split(config.inputPath)
  } yield ()
}
