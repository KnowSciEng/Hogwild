package model

import dataset.Dataset
import utils.Utils

case class StoppingCriteria(dataset: Dataset, earlyStopping: Int, minLoss: Double) {

  private var bestWeights = SparseNumVector.empty[Double]
  private var bestLoss = Double.MaxValue
  private var losses: Seq[Double] = Seq.empty
  private var bestAccuracy = 0d
  private var accuracies: Seq[Double] = Seq.empty
  private var timesWithoutImproving = 0

  def compute(svm: SVM, displayLoss: Boolean): (Double, Double) = {
    val (loss, accuracy) = svm.lossAndAccuracy(dataset.testSet, dataset.inverseTidCountsVector)
    losses +:= loss
    if (displayLoss) {
      println(s"[LOSS = $loss][ACCURACY = ${accuracy * 100} %]")
    }
    if (loss < bestLoss) {
      bestLoss = loss
      bestAccuracy = accuracy
      bestWeights = svm.weights
    } else {
      println("[LOSS] my loss did not improve! :'(  (crying alone)")
      timesWithoutImproving += 1
    }
    loss -> accuracy
  }

  def shouldStop: Boolean = {
    bestLoss < minLoss || timesWithoutImproving > earlyStopping
  }

  def getWeights: SparseNumVector[Double] = bestWeights

  def getLoss: Double = bestLoss

  def getAccuracy: Double = bestAccuracy

  def export(): Unit = Seq(
    ("weights.csv", bestWeights.toMap.iterator.map{case (tid, value) => s"$tid, $value"}, "\n"),
    ("losses.csv", losses.iterator.map(_.toString), ", "),
    ("accuracy.csv", accuracies.iterator.map(_.toString), ", ")
  ).foreach{ case(filename, lines, sep) =>
    Utils.upload(filename, lines, sep)
  }
}