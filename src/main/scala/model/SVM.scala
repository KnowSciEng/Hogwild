package model

import utils.Label
import utils.Label.Label
import utils.Types.{Counts, LearningRate}


class SVM(lambda: Double, stepSize: LearningRate) {
  var weights: SparseNumVector[Double] = SparseNumVector.empty

  /**
    * update the weights of the model and return the weights coordinates by how much they've changed
    *
    * @param gradient
    * @return weights update
    */
  def updateWeights(gradient: SparseNumVector[Double]): SparseNumVector[Double] = {
    val weightsUpdate = SparseNumVector(
      gradient.tids.map { k =>
        val weightUpdate = -stepSize * gradient.toMap(k)
        k -> weightUpdate
      }.toMap
    )
    addWeightsUpdate(weightsUpdate)
    weightsUpdate
  }

  def addWeightsUpdate(weightsUpdate: SparseNumVector[Double]): Unit = {
    weights += weightsUpdate
  }

  def predict(features: IndexedSeq[SparseNumVector[Double]]): IndexedSeq[Double] = {
    features.map(_ dot weights)
  }

  def predictLabels(features: IndexedSeq[SparseNumVector[Double]]): IndexedSeq[Label] = {
    predict(features).map(pred => Label.fromInt(Math.round(pred).toInt))
  }

  def lossAndAccuracy(features: IndexedSeq[SparseNumVector[Double]], labels: IndexedSeq[Label], tidCounts: Counts): (Double, Double) = {
    require(features.size == labels.size)
    val inverseTidCountsVector = SparseNumVector(tidCounts.mapValues(1d / _))

    val (losses, correctPredictions) = features.zip(labels)
      .map { case (feature, label) =>
        val pred = feature dot weights
        val hinge = Math.max(0, 1 - (label.id * pred))
        val w = weights.filterKeys(feature.tids)
        val reg = 0.5 * lambda * (w * w * inverseTidCountsVector).firstNorm
        val loss = hinge + reg
        val correctPred = Math.abs(pred.toInt + label.id) / 2
        loss -> correctPred
      }.unzip

    val accuracy = correctPredictions.sum / correctPredictions.length.toDouble
    val loss = losses.sum / losses.length.toDouble

    loss -> accuracy
  }

  def computeStochasticGradient(feature: SparseNumVector[Double],
                                label: Label,
                                tidCounts: Counts): SparseNumVector[Double] = {
    SVM.computeStochasticGradient(feature, label, weights, lambda, tidCounts)
  }
}

object SVM {
  def computeStochasticGradient(feature: SparseNumVector[Double],
                                label: Label,
                                weights: SparseNumVector[Double],
                                lambda: Double,
                                tidCounts: Counts): SparseNumVector[Double] = {

    val inverseTidCountsVector = SparseNumVector(tidCounts.mapValues(1d / _))
    val gradRightPart = weights.filterKeys(feature.tids) * lambda * inverseTidCountsVector
    if (label.id * (feature dot weights) >= 1) {
      gradRightPart
    } else {
      gradRightPart + (feature * (-label.id))
    }
  }
}
