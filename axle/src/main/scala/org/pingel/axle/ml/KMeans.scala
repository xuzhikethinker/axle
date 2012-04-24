package org.pingel.axle.ml

import org.pingel.axle.matrix.JblasMatrixFactory.JblasMatrix
import org.pingel.axle.matrix.JblasMatrixFactory.double2double
import org.pingel.axle.matrix.JblasMatrixFactory.rand

class KMeans {

  import org.pingel.axle.matrix.JblasMatrixFactory._

  // X is NOT left-padded with 1's for k-means clustering
  
  def cluster(K: Int, X: JblasMatrix[Double], iterations: Int): (JblasMatrix[Double], JblasMatrix[Double]) = {

    val n = X.columns
    val m = X.rows

    // assert: K < m
    
    // TODO: normalize X

    var centroids = rand[Double](K, n) // random initial K centroids μ in R^n (aka M)

    var C = rand[Double](1, 1) // TODO indexes of centroids closest to xi
    
    (0 until iterations).map(x => {
      (0 until m).map(i => {
    	  // TODO ci = index of centroid closest to xi
      })
      (0 until K).map(k => {
    	  // TODO μk = average of points assigned to cluster k
      })
    })

    (centroids, C)
  }

}

// http://en.wikipedia.org/wiki/Greek_alphabet