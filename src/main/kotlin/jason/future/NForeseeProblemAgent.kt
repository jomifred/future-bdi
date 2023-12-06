package jason.future

import jason.asSyntax.*
import jason.mas2j.AgentParameters
import jason.runtime.Settings.PROJECT_PARAMETER
import npl.NPLInterpreter
import npl.NormativeProgram
import npl.StateTransitions
import npl.parser.nplp
import java.io.FileReader

/**
 *  agent that considers the future
 *  and problems like loops in the behaviour/goal not achieved
 *  and norm violations
 */
open class NForeseeProblemAgent : ForeseeProblemAgent() {

    private val interpreter = NPLInterpreter()

    override fun initAg() {
        super.initAg()

        try {
            val agC = (ts.settings.userParameters[PROJECT_PARAMETER] as AgentParameters).agClass
            if (agC.parameters.isNotEmpty()) {
                var nplFileName = agC.parameters.first()
                nplFileName = nplFileName.substring(1..nplFileName.length-2)
                println("*** loading norms from $nplFileName")

                val p = NormativeProgram()
                nplp(FileReader(nplFileName)).program(p, null)

                interpreter.setStateManager(StateTransitions(interpreter))
                interpreter.init()
                interpreter.loadNP(p.root)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun failure(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop: Boolean, env: EnvironmentModel<State,Action>): Literal? {
        val myview = env.agPerception(this.getTS().agArch.agName)

        /*
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
                val ltzpos = Location(
                    (l.getTerm(0) as NumberTerm).solve().toInt(),
                    (l.getTerm(1) as NumberTerm).solve().toInt())
                if (ltzpos.equals(pos)) {
                    println("***** $pos in in LTZ!!!!!")
                    //return true;
                }
            }
        }
        */

        interpreter.clearFacts()
        for (f in myview)
            interpreter.addFact(f)
        interpreter.verifyNorms()
        //println("facts: "+interpreter.facts)
        //println("active: "+interpreter.active.size)
        //println("unfulfilled: "+interpreter.unFulfilled.size)
        val unfull = interpreter.unFulfilled
        if (unfull.isNotEmpty()) {
            //logger.info("unfulfilled norm: $unfull")
            val norms = ListTermImpl()
            for (ni in unfull) {
                norms.add(ASSyntax.createLiteral("norm",
                    Atom(ni.norm.id),
                    ni.unifierAsTerm
                ))
            }
            return ASSyntax.createLiteral("norm_unfulfilled", norms)
                //ASSyntax.createList(unfull as Collection<Term>?))

        }
        return super.failure(history, steps, stepsWithoutAct, hasLoop, env)
    }


}
