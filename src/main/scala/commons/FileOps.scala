package commons

import zio.{Task, ZIO}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

object FileOps {
  def write(path: String, content: Array[Int]): Task[Path] = ZIO.attempt {
    Files.write(
      Paths.get(path),
      content.mkString("\n").getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
  }
}
