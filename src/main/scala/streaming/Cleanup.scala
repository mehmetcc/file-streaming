package streaming

import commons.FolderOps
import zio.{RIO, ULayer, ZIO, ZLayer}

object Cleanup {
  def run: RIO[Configuration, Unit] = ZIO.serviceWith[Configuration](_.intermediaryDirectory).flatMap(FolderOps.delete)

  val live: ULayer[Unit] = ZLayer.succeed(())
}
