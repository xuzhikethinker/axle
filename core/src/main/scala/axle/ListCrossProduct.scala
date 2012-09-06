package axle

import collection._

class ListCrossProduct[E](lists: Seq[List[E]]) extends CrossProduct[E](lists) {

  val mults = lists.reverse.map(_.size).scanLeft(1)(_ * _).reverse

  val syze = mults.head

  val modulos = mults.tail

  def indexOf(objects: List[E]): Int =
    lists.zip(objects).map(lo => lo._1.indexOf(lo._2)).zip(modulos).map(im => im._1 * im._2).sum

  def apply(i: Int): List[E] =
    lists.zip(modulos).foldLeft((i, List[E]()))((cr, lm) => (cr._1 % lm._2, lm._1(cr._1 / lm._2) :: cr._2))._2.reverse

  override def size() = syze

}
