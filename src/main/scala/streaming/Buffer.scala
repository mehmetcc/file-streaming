package streaming

import zio._
import zio.stm.TPriorityQueue

import scala.language.postfixOps

trait Buffer {
  def enqueueAll(items: List[String]): UIO[Unit]

  def dequeueAll: UIO[List[String]]
}

object Buffer {
  def enqueueAll(items: List[String]): URIO[Buffer, Unit] = ZIO.serviceWithZIO[Buffer](_.enqueueAll(items))

  def dequeueAll: URIO[Buffer, List[String]] = ZIO.serviceWithZIO[Buffer](_.dequeueAll)
}

case class BufferImpl(queue: TPriorityQueue[String]) extends Buffer {
  override def dequeueAll: UIO[List[String]] = queue.takeAll.commit.map(_.toList)

  override def enqueueAll(items: List[String]): UIO[Unit] = queue.offerAll(items).commit
}

object BufferImpl {
  val live: ULayer[Buffer] = ZLayer {
    TPriorityQueue.empty[String].commit.map(BufferImpl(_))
  }
}
