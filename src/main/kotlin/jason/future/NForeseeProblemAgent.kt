package jason.future

import jason.asSyntax.NumberTerm
import jason.environment.grid.Location

/**
 *  agent that considers the future
 *  and problems like loops in the behaviour/goal not achieved
 *  and norm violations
 */
open class NForeseeProblemAgent : ForeseeProblemAgent() {

    override fun failure(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop: Boolean, env: EnvironmentModel<State,Action>): Boolean {
        val myview = env.agPerception(this.getTS().agArch.agName)
        // find pos
        var pos = Location(-1,-1)
        for (l in myview) {
            if (l.functor == "pos") {
                pos = Location(
                    (l.getTerm(0) as NumberTerm).solve().toInt(),
                    (l.getTerm(1) as NumberTerm).solve().toInt())
            }
        }
        // check LTZ
        for (l in myview) {
            if (l.functor == "ltz") {
                var ltzpos = Location(
                    (l.getTerm(0) as NumberTerm).solve().toInt(),
                    (l.getTerm(1) as NumberTerm).solve().toInt())
                if (ltzpos.equals(pos)) {
                    println("***** $pos in in LTZ!!!!!")
                    return true;
                }
            }
        }


        return super.failure(history, steps, stepsWithoutAct, hasLoop, env)
    }

    override fun initAg() {
        super.initAg()
    }

}
