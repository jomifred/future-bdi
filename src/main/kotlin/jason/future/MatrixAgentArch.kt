package jason.future

import jason.architecture.AgArch
import jason.asSemantics.ActionExec
import jason.asSemantics.Option
import jason.asSyntax.Literal
import jason.asSyntax.Structure

/** agent that run in the "Matrix" (simulated world) */
class MatrixAgentArch (
    val env : EnvironmentModel<State, Action>,
    private val agName : String
) : AgArch() {

    val historyS = mutableListOf<State>()
    val historyO = mutableListOf<Option>()
    val historyA = mutableListOf<Structure>()

    var myFO : FutureOption? = null

    init {
        historyS.add( env.currentState() )
    }

    override fun getAgName(): String {
        return agName
    }

    fun getAg() = ts.ag as MatrixAgent

    // main ag is the one starting the matrix to discover something
    fun isMainAg() = ts.ag is MatrixAgent

    override fun perceive(): MutableCollection<Literal> {
        //println("        matrix perception: ${env.agPerception(agName)} for $agName")
        return env.agPerception(agName)
    }

    override fun act(action: ActionExec) {
        //println("        matrix action: ${action.actionTerm} from ${agName}")
        if (isMainAg()) {
            if (ts.c.selectedOption != null)
                historyO.add(ts.c.selectedOption)
            if (getAg().firstSO || getAg().inZone1)
                getAg().lastFO?.actions?.add(action.actionTerm)
            historyA.add(action.actionTerm)
        }

        val newState = env.execute( env.structureToAction(agName, action.actionTerm) )
        action.result = true
        actionExecuted(action)

        if (isMainAg()) {
            historyS.add(newState)
        }
    }
}