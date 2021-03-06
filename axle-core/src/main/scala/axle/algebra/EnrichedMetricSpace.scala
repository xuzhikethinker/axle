package axle.algebra

import spire.math._
import spire.implicits._
import spire.algebra.MetricSpace
import axle._
import collection._

class EnrichedMetricSpace[T: Manifest](space: MetricSpace[T, Double]) {

  import axle.matrix.JblasMatrixModule._
  import space._

  def distanceMatrix(vectors: IndexedSeq[T]): Matrix[Double] = {
    val n = vectors.size
    matrix(n, n, (r: Int, c: Int) => distance(vectors(r), vectors(c)).toDouble)
  }

  def nMostSimilar(query: T, vectors: Iterator[T], c: Int): List[(Int, Double)] =
    vectors.zipWithIndex
      .map({ case (v, i) => (i, distance(query, v)) })
      .toList
      .sortBy(_._2)
      .take(c)

  //  def nMostSimilarReport(query: String, n: Int) = nMostSimilar(query, n)
  //    .map(is => (is._2, corpus(is._1))).map(sd => "%.4f %s".format(sd._1, sd._2)).mkString("\n")

  /**
   * triangleInequalityHolds
   *
   * Applies the Triangle Inequality using all the triples formed by the
   * given data to see if this is a true "distance"
   *
   * http://en.wikipedia.org/wiki/Triangle_inequality
   */

  def triangleInequalityHolds(data: GenTraversable[T]): Boolean =
    data.triples.forall({ case (a, b, c) => distance(a, b) + distance(b, c) >= distance(a, c) })

}

