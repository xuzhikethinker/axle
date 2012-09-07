package axle

import collection._

/**
 * Based on Python's itertools.permutations function
 *
 * http://docs.python.org/library/itertools.html#itertools.permutations
 *
 * Permutations("ABCD".toList, 2)
 * Permutations(0 until 3)
 *
 */

object Permutations {

  def apply[E](objects: IndexedSeq[E], rOpt: Option[Int] = None): Permutations[E] = new Permutations[E](objects, rOpt)
}

class Permutations[E](pool: IndexedSeq[E], rOpt: Option[Int]) extends Iterable[List[E]] {

  val n = pool.length

  val r = rOpt.getOrElse(n)

  override def size() = if (r >= 0 && r <= n) { n.factorial / (n - r).factorial } else { 0 }

  val yeeld = new mutable.ListBuffer[List[E]]() // TODO substitute for "yield" for now

  if (r <= n) {
    val indices = (0 until n).toBuffer
    val cycles = n.until(n - r, -1).toBuffer
    yeeld += indices(0 until r).map(pool(_)).toList
    var done = false
    while (n > 0 && !done) {
      var i = r - 1
      var broken = false
      while (i >= 0 && !broken) {
        cycles(i) -= 1
        if (cycles(i) == 0) {
          indices(i until n) = indices(i + 1 until n) ++ indices(i until i + 1)
          cycles(i) = n - i
        } else {
          val j = cycles(i)
          val (v1, v2) = (indices((n - j) % n), indices(i))
          indices(i) = v1
          indices((n - j) % n) = v2
          yeeld += indices(0 until r).map(pool(_)).toList
          broken = true
        }
        if (!broken) {
          i -= 1
        }
      }
      if (!broken) {
        done = true
      }
    }
  }

  def iterator() = yeeld.iterator

}
