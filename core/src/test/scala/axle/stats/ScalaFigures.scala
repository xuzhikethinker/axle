
package org.pingel.bayes.examples

import collection._
import axle.stats._
import axle.visualize._
import axle.graph.JungDirectedGraphFactory._
import axle.graph.JungUndirectedGraphFactory.JungUndirectedGraph
import org.specs2.mutable._

class ScalaFigures extends Specification {

  val bools = Some(Vector(true, false))

  val A = new RandomVariable0("A", bools, None)
  val B = new RandomVariable0("B", bools, None)
  val C = new RandomVariable0("C", bools, None)
  val D = new RandomVariable0("D", bools, None)
  val E = new RandomVariable0("E", bools, None)

  def figure6_1() = {

    val bn = BayesianNetwork("6.1")

    val cptA = Factor(Vector(A)) // A
    cptA(List(A eq true)) = 0.6
    cptA(List(A eq false)) = 0.4
    val av = bn += BayesianNetworkNode(A, cptA)

    val cptB = Factor(Vector(B, A)) // B | A
    cptB(List(B eq true, A eq true)) = 0.2
    cptB(List(B eq true, A eq false)) = 0.8
    cptB(List(B eq false, A eq true)) = 0.75
    cptB(List(B eq false, A eq false)) = 0.25
    val bv = bn += BayesianNetworkNode(B, cptB)

    val cptC = Factor(Vector(C, A)) // C | A
    cptC(List(C eq true, A eq true)) = 0.8
    cptC(List(C eq true, A eq false)) = 0.2
    cptC(List(C eq false, A eq true)) = 0.1
    cptC(List(C eq false, A eq false)) = 0.9
    val cv = bn += BayesianNetworkNode(C, cptC)

    val cptD = Factor(Vector(D, B, C)) // D | BC
    cptD(List(D eq true, B eq true, C eq true)) = 0.95
    cptD(List(D eq true, B eq true, C eq false)) = 0.05
    cptD(List(D eq true, B eq false, C eq true)) = 0.9
    cptD(List(D eq true, B eq false, C eq false)) = 0.1
    cptD(List(D eq false, B eq true, C eq true)) = 0.8
    cptD(List(D eq false, B eq true, C eq false)) = 0.2
    cptD(List(D eq false, B eq false, C eq true)) = 0.0
    cptD(List(D eq false, B eq false, C eq false)) = 1.0
    val dv = bn += BayesianNetworkNode(D, cptD)

    val cptE = Factor(Vector(E, C)) // E | C
    cptE(List(E eq true, C eq true)) = 0.7
    cptE(List(E eq true, C eq false)) = 0.3
    cptE(List(E eq false, C eq true)) = 0.0
    cptE(List(E eq false, C eq false)) = 1.0
    val ev = bn += BayesianNetworkNode(E, cptE)

    bn += (av -> bv, "")
    bn += (av -> cv, "")
    bn += (bv -> dv, "")
    bn += (cv -> dv, "")
    bn += (cv -> ev, "")

    bn
  }

  def figure6_2() = figure6_1.getJointProbabilityTable()

  def figure6_3() = {

    val cptB = Factor(B :: C :: D :: Nil) //Figure 3.1
    cptB(List(B eq true, C eq true, D eq true)) = 0.95
    cptB(List(B eq true, C eq true, D eq false)) = 0.05
    cptB(List(B eq true, C eq false, D eq true)) = 0.9
    cptB(List(B eq true, C eq false, D eq false)) = 0.1
    cptB(List(B eq false, C eq true, D eq true)) = 0.8
    cptB(List(B eq false, C eq true, D eq false)) = 0.2
    cptB(List(B eq false, C eq false, D eq true)) = 0.0
    cptB(List(B eq false, C eq false, D eq false)) = 1.0

    val cptD = Factor(D :: E :: Nil) // Figure 3.2
    cptD(List(D eq true, E eq true)) = 0.448
    cptD(List(D eq true, E eq false)) = 0.192
    cptD(List(D eq false, E eq true)) = 0.112
    cptD(List(D eq false, E eq false)) = 0.248

    val h = (cptB.sumOut(D)).sumOut(C)
    val m = cptB * cptD

    (cptB, cptD)
  }

  def figure6_4() = {

    val bn = new BayesianNetwork("6.4")

    val cptA = Factor(Vector(A))
    cptA(List(A eq true)) = 0.6
    cptA(List(A eq false)) = 0.4
    val av = bn += BayesianNetworkNode(A, cptA)

    val cptB = Factor(Vector(B)) // B | A
    cptB(List(B eq true, A eq true)) = 0.9
    cptB(List(B eq true, A eq false)) = 0.1
    cptB(List(B eq false, A eq true)) = 0.2
    cptB(List(B eq false, A eq false)) = 0.8
    val bv = bn += BayesianNetworkNode(B, cptB)

    val cptC = Factor(Vector(C)) // C | B
    cptC(List(C eq true, B eq true)) = 0.3
    cptC(List(C eq true, B eq false)) = 0.7
    cptC(List(C eq false, B eq true)) = 0.5
    cptC(List(C eq false, B eq false)) = 0.5
    val cv = bn += BayesianNetworkNode(C, cptC)

    bn += (av -> bv, "")
    bn += (bv -> cv, "")

    val pB = (((cptB * cptA).sumOut(A)) * cptC).sumOut(C)

    bn
  }

  def figure6_5(): List[InteractionGraph] =
    figure6_1().interactionGraph().eliminationSequence(List(B, C, A, D))

  def figure6_7() = {

    val f61 = figure6_1()

    // Figure 6.1 pruned towards B & E
    val Q1: immutable.Set[RandomVariable[_]] = immutable.Set(B, E)
    val f67pBE = f61.pruneNetworkVarsAndEdges(Q1, None)

    // Figure 6.2 pruned towards B
    val Q2: immutable.Set[RandomVariable[_]] = immutable.Set(B)
    val f67pB = f61.pruneNetworkVarsAndEdges(Q2, None)

    (f67pBE, f67pB)
  }

  // Figure 6.1 with edges pruned towards C=false
  def figure6_8() = figure6_1().pruneEdges("Figure 6.8", Some(List(C eq false)))

  // Figure 6.1 pruned towards Q={D} and A=true,C=false
  def figure6_9() =
    figure6_1().pruneNetworkVarsAndEdges(Set(D), Some(List(A eq true, C eq false)))

  // Result of fe-i on a->b->c with Q={C}
  def figure7_2() = {
    // TODO: needs to be immutable
    val f72 = figure6_4.duplicate()
    val f = f72.factorElimination1(Set(C))
    f72
  }

  def figure7_4() = {

    val f61 = figure6_1()

    val τ = new EliminationTree()

    val τ_vA = τ.g += f61.getCPT(A)
    val τ_vB = τ.g += f61.getCPT(B)
    val τ_vC = τ.g += f61.getCPT(C)
    val τ_vD = τ.g += f61.getCPT(D)
    val τ_vE = τ.g += f61.getCPT(E)

    τ.g += (τ_vA -> τ_vB, "")
    τ.g += (τ_vA -> τ_vD, "")
    τ.g += (τ_vD -> τ_vC, "")
    τ.g += (τ_vC -> τ_vE, "")

    // factorElimination2 on figure6.1 with Q={C} and τ={...} and r=n3
    val (f68, elim) = f61.factorElimination2(Set(C), τ, f61.getCPT(C))
    (f68, τ, f61.getCPT(C))
  }

  // factorElimination3 on figure6.1 with Q={C} and τ={...} and r=n3
  def figure7_5() = {
    // TODO: needs to be immutable
    val f61 = figure6_1()
    val (bn, τ, cptC) = figure7_4()
    val (f75, elim) = f61.factorElimination2(Set(C), τ, cptC)
    f61
  }

  def figure7_12() = {
    import axle.graph.JungUndirectedGraphFactory._
    val g = graph[mutable.Set[RandomVariable[_]], String]()
    val vABC = g += mutable.Set(A, B, C)
    val vBCD = g += mutable.Set(B, C, D)
    val vCE = g += mutable.Set(C, E)
    g += (vABC -> vBCD, "")
    g += (vBCD -> vCE, "")
    new JoinTree(g)
  }

  "bayesian networks" should {
    "work" in {

      1 must be equalTo 1
    }
  }

}
