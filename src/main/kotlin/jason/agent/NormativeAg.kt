package jason.agent

import jason.architecture.AgArch
import jason.asSemantics.Agent
import jason.asSyntax.Literal
import jason.asSyntax.PredicateIndicator
import jason.mas2j.AgentParameters
import jason.runtime.Settings
import npl.NPLInterpreter
import npl.NormInstance
import npl.NormativeProgram
import npl.StateTransitions
import npl.parser.nplp
import java.io.FileReader

/** agent that has a Normative Reasoning Module in its mind */
open class NormativeAg : PreferenceAgent {

    var program: NormativeProgram? = null

    constructor()

    constructor(program: NormativeProgram?) {
        this.program = program
    }

    private val interpreter = NPLInterpreter()

    private val piUnfulfilled = PredicateIndicator("unfulfilled",1)

    override fun initAg() {
        super.initAg()
        try {
            if (program == null) {
                val agC = (ts.settings.userParameters[Settings.PROJECT_PARAMETER] as AgentParameters).agClass
                if (agC.parameters.isNotEmpty()) {
                    var nplFileName = agC.parameters.first()
                    nplFileName = nplFileName.substring(1..nplFileName.length - 2)
                    logger.info("*** loading norms from $nplFileName")

                    program = NormativeProgram()
                    nplp(FileReader(nplFileName)).program(program, null)
                }
            }
            interpreter.setStateManager(StateTransitions(interpreter))
            interpreter.setAg(this)
            interpreter.init()
            interpreter.loadNP(program?.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun myUnfulfilledNorms() : List<NormInstance> {
        val unList = mutableListOf<NormInstance>()
        val unFuls = bb.getCandidateBeliefs(piUnfulfilled)
        if (unFuls != null) {
            for (belUnFul in unFuls) {
                val unf = belUnFul.getTerm(0) as NormInstance
                if (!unf.ag.isGround || ts.agArch.agName.startsWith(unf.ag.toString())) // need to be startWith because of matrix agents names
                    unList.add(unf)
            }
            //logger.info("unfuls "+unList)
        }
        return unList
    }

    fun resetNPL() {
        interpreter.clearFacts()
    }

    override fun stopAg() {
        super.stopAg()
        interpreter.stop()
    }

    override fun buf(percepts: MutableCollection<Literal>?): Int {
        val r = super.buf(percepts)
        interpreter.verifyNorms()
        //logger.info("unfuls: "+interpreter.unFulfilled)
        return r
    }

    override fun cloneInto(arch: AgArch?, a: Agent?): Agent {
        val newAg = super.cloneInto(arch, a)
        if (newAg is NormativeAg) {
            newAg.interpreter.setAllActivatedNorms( interpreter.activatedNorms)
        }
        return newAg
    }
}