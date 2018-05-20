package grpc.sync

import model.SparseNumVector


class WorkersAggregator {
  private var numWorkers = 0
  private var gradients: List[SparseNumVector[Double]] = List()

  def addWorker(): Unit = {
    numWorkers += 1
  }

  def removeWorker(): Unit = {
    numWorkers -= 1
  }

  def isWaitingOnSomeWorker: Boolean = {
    gradients.size < numWorkers
  }

  def addGradient(gradient: SparseNumVector[Double]): Unit = {
    gradients ::= gradient
  }

  def getMeanGradient: SparseNumVector[Double] = {
    val meanGradient = gradients.reduce(_ + _).mapTo((k, v) => v / gradients.size)
    gradients = List()
    meanGradient
  }

}