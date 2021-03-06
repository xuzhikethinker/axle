package axle

import collection._
import math.{ exp, log }

case class EnrichedGenTraversable[+T : Manifest](gt: GenTraversable[T]) {

  def Σ(f: T => Double) = gt.aggregate(0.0)(_ + f(_), _ + _)

  def Sigma(f: T => Double) = Σ(f)

  def Πx(f: T => Double): Double = exp(gt.map(x => log(f(x))).sum) // TODO: use aggregate for sum?

  def Π(f: T => (() => Double)): Double = gt.aggregate(1.0)((a, b) => a * f(b)(), (x, y) => x * y)

  def Pi(f: T => (() => Double)) = Π(f)

  //  def Π(f: T => (() => Double)): Double = gt.aggregate(1.0)((a, b) => a * f(b)(), (x, y) => x * y)
  //
  //  def Pi(f: T => (() => Double)) = Π(f)

  def ∀(p: T => Boolean) = gt.forall(p)

  def ∃(p: T => Boolean) = gt.exists(p)

  //  def doubles(): GenTraversable[(T, T)] = for (x <- gt; y <- gt) yield (x, y)
  //  
  //  def triples(): GenTraversable[(T, T, T)] = for (x <- gt; y <- gt; z <- gt) yield (x, y, z)

  def doubles(): Seq[(T, T)] = gt.toIndexedSeq.permutations(2).map(d => (d(0), d(1))).toSeq

  def triples(): Seq[(T, T, T)] = gt.toIndexedSeq.permutations(3).map(t => (t(0), t(1), t(2))).toSeq

  def ⨯[S](right: GenTraversable[S]) = for (x <- gt; y <- right) yield (x, y)

}
