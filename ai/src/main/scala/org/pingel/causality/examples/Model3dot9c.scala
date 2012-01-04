
package org.pingel.causality.examples

import org.pingel.causality.CausalModel
import org.pingel.causality.Function
import org.pingel.bayes.ModelVisualizer
import org.pingel.bayes.RandomVariable

object Model3dot9c extends CausalModel("3.9c") {

	val X = addVariable(new RandomVariable("X"))
	val Y = addVariable(new RandomVariable("Y"))
	val Z = addVariable(new RandomVariable("Z"))
	val U1 = addVariable(new RandomVariable("U1", None, false))
		
	addFunction(new Function(X, List(U1)))
	addFunction(new Function(Y, List(X, Z)))
	addFunction(new Function(Z, List(X, U1)))

    def main(args: Array[String]) {
        ModelVisualizer.draw(Model3dot9c)
    }

}