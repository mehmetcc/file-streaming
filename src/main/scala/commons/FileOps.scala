package commons

import zio.{Task, ZIO}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}

object FileOps {
  def write(path: String, content: Array[Int]): Task[Path] = PathOps.of(path).flatMap(write(_, content))

  def write(path: Path, content: Array[Int]): Task[Path] = ZIO.attempt {
    Files.write(
      path,
      content.mkString("\n").getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
  }

  def append(path: String, content: Array[Int]): Task[Path] = PathOps.of(path).flatMap(append(_, content))

  def append(path: Path, content: Array[Int]): Task[Path] = ZIO.attempt {
    Files.write(
      path,
      content.mkString("\n").appended('\n').getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE,
      StandardOpenOption.APPEND
    )
  }
}
