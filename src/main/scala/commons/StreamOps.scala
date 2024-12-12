package commons

import zio.Trace
import zio.stream.ZStream

object StreamOps {
  def mergeSortedStreams[R, E, A](
    streams: List[ZStream[R, E, A]]
  )(implicit ord: Ordering[A], trace: Trace): ZStream[R, E, A] =
    if (streams.isEmpty) ZStream.empty
    else if (streams.size == 1) streams.head
    else streams.tail.foldLeft(streams.head) { case (acc, stream) => acc.mergeSorted(stream) }
}
