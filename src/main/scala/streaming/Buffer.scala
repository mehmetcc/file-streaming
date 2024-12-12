package streaming

import zio._
import zio.stm.TPriorityQueue

import scala.language.postfixOps

trait Buffer {
  def enqueueAll(items: List[Int]): UIO[Unit]

  def dequeueAll: UIO[List[Int]]
}

object Buffer {
  def enqueueAll(items: List[Int]): URIO[Buffer, Unit] = ZIO.serviceWithZIO[Buffer](_.enqueueAll(items))

  def dequeueAll: URIO[Buffer, List[Int]] = ZIO.serviceWithZIO[Buffer](_.dequeueAll)
}

case class BufferImpl(queue: TPriorityQueue[Int]) extends Buffer {
  override def dequeueAll: UIO[List[Int]] = queue.takeAll.commit.map(_.toList)

  override def enqueueAll(items: List[Int]): UIO[Unit] = queue.offerAll(items).commit
}

object BufferImpl {
  val live: ULayer[Buffer] = ZLayer {
    TPriorityQueue.empty[Int].commit.map(BufferImpl(_))
  }
}
