package org.pingel.axle.graph

import scala.collection._

object NativeUndirectedGraphFactory extends NativeUndirectedGraphFactory

trait NativeUndirectedGraphFactory extends UndirectedGraphFactory {

  type G = NativeUndirectedGraph[_, _]

  def graph[VP, EP]() = new NativeUndirectedGraph[VP, EP]() {}

  trait NativeUndirectedGraph[VP, EP] extends UndirectedGraph[VP, EP] {

    import scala.collection._

    type V = NativeUndirectedGraphVertex

    type E = NativeUndirectedGraphEdge

    type S = (mutable.Set[V], mutable.Set[E], Map[V, mutable.Set[E]])

    var vertices = mutable.Set[V]()
    var edges = mutable.Set[E]()
    var vertex2edges = mutable.Map[V, mutable.Set[E]]()

    def getStorage() = (vertices, edges, vertex2edges)

    def getVertices() = vertices.toSet

    def getEdges() = edges.toSet

    def size() = vertices.size

    trait NativeUndirectedGraphVertex extends UndirectedGraphVertex

    trait NativeUndirectedGraphEdge extends UndirectedGraphEdge

    class NativeUndirectedGraphVertexImpl(payload: VP) extends NativeUndirectedGraphVertex {

      self: V =>

      vertices += this

      def getPayload(): VP = payload
    }

    class NativeUndirectedGraphEdgeImpl(v1: V, v2: V, payload: EP) extends NativeUndirectedGraphEdge {

      self: E =>

      // assume that this edge isn't already in our list of edges
      edges += this
      var es1 = getEdges(v1)
      es1.add(this)
      var es2 = getEdges(v2)
      es2.add(this)

      def getVertices(): (V, V) = (v1, v2)
      def getPayload(): EP = payload
    }

    def vertex(payload: VP): NativeUndirectedGraphVertex = new NativeUndirectedGraphVertexImpl(payload)

    def edge(v1: V, v2: V, payload: EP): NativeUndirectedGraphEdge = new NativeUndirectedGraphEdgeImpl(v1, v2, payload)

    def copyTo(other: UndirectedGraph[VP, EP]) = {
      // TODO
    }

    def unlink(e: E): Unit = {

      val dble = e.getVertices()

      var es1 = getEdges(dble._1)
      es1.remove(e)

      var es2 = getEdges(dble._2)
      es2.remove(e)

      edges -= e
    }

    def unlink(v1: V, v2: V): Unit = getEdges(v1).filter(_.other(v1).equals(v2)).map(unlink(_))

    def areNeighbors(v1: V, v2: V) = getEdges(v1).exists(_.connects(v1, v2))

    override def isClique(vs: Set[V]): Boolean = {
      // vs.pairs().forall({ case (a, b) => ( (a == b) || areNeighbors(a, b) ) })
      var vList = mutable.ArrayBuffer[V]()
      vList ++= vs
      for (i <- 0 until vList.size) {
        for (j <- 0 until vList.size) {
          if (!areNeighbors(vList(i), vList(j))) {
            return false
          }
        }
      }
      true
    }

    override def getNumEdgesToForceClique(vs: Set[V], payload: (V, V) => EP) = {

      var N = mutable.ArrayBuffer[V]()
      N ++= vs

      var result = 0

      for (i <- 0 to (N.size - 2)) {
        val vi = N(i)
        for (j <- (i + 1) until N.size) {
          val vj = N(j)
          if (!areNeighbors(vi, vj)) {
            edge(vi, vj, payload(vi, vj))
            result += 1
          }
        }
      }

      result
    }

    override def forceClique(vs: Set[V], payload: (V, V) => EP): Unit = {

      var vList = mutable.ArrayBuffer[V]()
      vList ++= vs

      for (i <- 0 until (vList.size - 1)) {
        val vi = vList(i)
        for (j <- (i + 1) until vList.size) {
          val vj = vList(j)
          if (!areNeighbors(vi, vj)) {
            edge(vi, vj, payload(vi, vj))
          }
        }
      }

    }

    override def vertexWithFewestEdgesToEliminateAmong(among: Set[V], payload: (V, V) => EP): Option[V] = {

      // assert: among is a subset of vertices

      var result: Option[V] = None
      var minSoFar = Integer.MAX_VALUE

      for (v <- among) {
        val x = getNumEdgesToForceClique(getNeighbors(v), payload)
        if (result == None) {
          result = Some(v)
          minSoFar = x
        } else if (x < minSoFar) {
          result = Some(v)
          minSoFar = x
        }
      }
      result
    }

    override def vertexWithFewestNeighborsAmong(among: Set[V]): Option[V] = {
      // assert: among is a subset of vertices

      var result: Option[V] = None
      var minSoFar = Integer.MAX_VALUE

      for (v <- among) {
        val x = getNeighbors(v).size
        if (result == None) {
          result = Some(v)
          minSoFar = x
        } else if (x < minSoFar) {
          result = Some(v)
          minSoFar = x
        }
      }

      result
    }

    def degree(v: V) = getEdges(v).size

    def getEdges(v: V) = {
      if (!vertex2edges.contains(v)) {
        vertex2edges += v -> scala.collection.mutable.Set[E]()
      }
      vertex2edges(v)
    }

    def getNeighbors(v: V): Set[V] = getEdges(v).map(_.other(v)).toSet

    def delete(v: V) = {
      val es = getEdges(v)
      vertices -= v
      vertex2edges.remove(v)
      for (e <- es) {
        edges -= e
        vertex2edges.get(e.other(v)) map { otherEdges => otherEdges.remove(e) }
      }
    }

    // a "leaf" is vertex with only one neighbor
    def firstLeafOtherThan(r: V) = vertices.find({ v => getNeighbors(v).size == 1 && !v.equals(r) })

    def eliminate(v: V, payload: (V, V) => EP) = {
      // "decompositions" page 3 (Definition 3, Section 9.3)
      // turn the neighbors of v into a clique

      val es = getEdges(v)
      val vs = getNeighbors(v)

      vertices -= v
      vertex2edges.remove(v)
      edges --= es

      forceClique(vs.asInstanceOf[Set[V]], payload)
    }

    // TODO there is probably a more efficient way to do this:
    def eliminate(vs: immutable.List[V], payload: (V, V) => EP): Unit = vs.map(eliminate(_, payload))

    def draw(): Unit = {
      // TODO: remove this cast
      val thisAsUG = this.asInstanceOf[JungUndirectedGraphFactory.UndirectedGraph[VP, EP]]
      JungUndirectedGraphFactory.graphFrom[VP, EP, VP, EP](thisAsUG)(vp => vp, ep => ep).draw()
    }

  }

}