package commons

import zio.{Task, ZIO}

import java.nio.file.{Path, Paths}

object PathOps {
  def of(path: String): Task[Path] = ZIO.attempt {
    Paths.get(path)
  }
}
