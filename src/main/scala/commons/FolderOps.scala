package commons

import zio.{Task, ZIO}

import java.nio.file.{Files, Path}

object FolderOps {
  def create(path: String): Task[Option[Path]] = PathOps.of(path).flatMap(path => create(path))

  def create(path: Path): Task[Option[Path]] = ZIO.succeed {
    if (!Files.exists(path)) Some(Files.createDirectories(path))
    else None
  }
}
