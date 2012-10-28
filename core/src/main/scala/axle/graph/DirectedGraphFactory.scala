package axle.graph

import collection._

trait GenDirectedGraph[VP, EP] extends GenGraph[VP, EP] {

  type V <: DirectedGraphVertex[VP]
  type E <: DirectedGraphEdge[EP]

  trait DirectedGraphVertex[P] extends GraphVertex[P]

  trait DirectedGraphEdge[P] extends GraphEdge[P] {
    def source(): V
    def dest(): V
  }

  def findEdge(from: V, to: V): Option[E]
  def leaves(): Set[V]
  def neighbors(v: V): Set[V]
  def precedes(v1: V, v2: V): Boolean
  def predecessors(v: V): Set[V]
  def isLeaf(v: V): Boolean
  def successors(v: V): Set[V]
  def outputEdgesOf(v: V): Set[E]
  def descendantsIntersectsSet(v: V, s: Set[V]): Boolean

  def _descendants(v: V, result: mutable.Set[V]): Unit = {
    // inefficient
    if (!result.contains(v)) {
      result += v
      successors(v).map(_descendants(_, result))
    }
  }

  def descendants(v: V): immutable.Set[V] = {
    val result = mutable.Set[V]()
    _descendants(v, result)
    result.toSet
  }

  // inefficient
  def _ancestors(v: V, result: mutable.Set[V]): Unit = {
    if (!result.contains(v)) {
      result += v
      predecessors(v).map(_ancestors(_, result))
    }
  }

  def ancestors(v: V): immutable.Set[V] = {
    val result = mutable.Set[V]()
    _ancestors(v, result)
    result.toSet
  }

  def ancestors(vs: Set[V]): immutable.Set[V] = {
    val result = mutable.Set[V]()
    vs.map(_ancestors(_, result))
    result.toSet
  }

  def isAcyclic(): Boolean
  def shortestPath(source: V, goal: V): Option[List[E]]
  // def moralGraph(): UndirectedGraph[_, _] = null // TODO !!!

  def deleteEdge(e: E): GenDirectedGraph[VP, EP]
  def deleteVertex(v: V): GenDirectedGraph[VP, EP]
  def removeInputs(vs: Set[V]): GenDirectedGraph[VP, EP]
  def removeOutputs(vs: Set[V]): GenDirectedGraph[VP, EP]
  def removeSuccessor(v: V, successor: V): GenDirectedGraph[VP, EP]
  def removePredecessor(v: V, predecessor: V): GenDirectedGraph[VP, EP]
}

trait DirectedGraphFactory extends GraphFactory {


}
