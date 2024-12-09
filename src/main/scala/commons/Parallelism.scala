package commons

object Parallelism {
  def threadId: Long = Thread.currentThread().threadId()

  def availableCores: Int = Runtime.getRuntime.availableProcessors()
}
