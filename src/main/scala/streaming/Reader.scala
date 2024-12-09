package streaming

import commons.{FileOps, FolderOps, Parallelism}
import zio.stream.ZStream
import zio.{Chunk, RIO, Task, URLayer, ZIO, ZLayer}

import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

trait Reader {
  def split(path: String): RIO[Configuration, List[Path]]

  def process(chunk: Chunk[Byte], index: Int): RIO[Configuration, Path]
}

object Reader {
  def split(path: String): RIO[Configuration with Reader, List[Path]] = ZIO.serviceWithZIO[Reader](_.split(path))
}

case class ReaderImpl(configuration: Configuration) extends Reader {
  override def split(path: String): RIO[Configuration, List[Path]] =
    for {
      _ <- FolderOps.create(configuration.intermediaryDirectory)
      paths <- ZStream
                 .fromPath(Paths.get(path))
                 .grouped(configuration.chunkSize * 1024 * 1024)
                 .zipWithIndex
                 .mapZIOPar(Parallelism.availableCores) { case (chunk, index) => process(chunk, index.toInt) }
                 .runCollect
                 .map(_.toList)
    } yield paths

  override def process(chunk: Chunk[Byte], index: Int): RIO[Configuration, Path] = for {
    _     <- ZIO.log(s"Processing chunk $index with size ${chunk.size} bytes on Thread: ${Parallelism.threadId}")
    ints  <- parse(chunk)
    sorted = ints.sorted
    path  <- FileOps.write(path = s"${configuration.intermediaryDirectory}/tmp_$index", content = sorted)
  } yield path

  private def parse(chunk: Chunk[Byte]): Task[Array[Int]] = for {
    content <- ZIO.succeed(new String(chunk.toArray, StandardCharsets.UTF_8))
    decoded <- separate(content)
  } yield decoded

  private def separate(source: String): Task[Array[Int]] = ZIO.attempt {
    source
      .split("\\s+")
      .filter(_.nonEmpty)
      .map(_.toInt)
  }
}

object ReaderImpl {
  val live: URLayer[Configuration, ReaderImpl] = ZLayer {
    ZIO.serviceWith[Configuration](ReaderImpl(_))
  }
}
