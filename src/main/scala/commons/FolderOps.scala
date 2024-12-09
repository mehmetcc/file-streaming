package commons

import zio.{Task, ZIO}

import java.nio.file.{Files, Path}
import java.util.Comparator

object FolderOps {
  def create(path: String): Task[Option[Path]] = PathOps.of(path).flatMap(create)

  def create(path: Path): Task[Option[Path]] = ZIO.succeed {
    if (!Files.exists(path)) Some(Files.createDirectories(path))
    else None
  }

  def delete(path: String): Task[Unit] = PathOps.of(path).flatMap(delete)

  def delete(path: Path): Task[Unit] = ZIO.attempt {
    Files
      .walk(path)
      .sorted(Comparator.reverseOrder())
      .forEach(Files.delete _)
  }
}
