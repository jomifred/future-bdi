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

    override fun perceive(): MutableCollection<Literal> {
        //println("        matrix perception: ${env.agPerception(agName)} for $agName")
        return env.agPerception(agName)
    }

    override fun act(action: ActionExec) {
        //println("        matrix action: ${action.actionTerm} from ${agName}")
        if (ts.c.selectedOption != null)
            historyO.add(ts.c.selectedOption)
        if (myFO?.ag?.firstSO == true || myFO?.ag?.inZone1 == true)
            myFO?.actions?.add(action.actionTerm) // store action of the option (not is sub-options)

        historyA.add(action.actionTerm)

        val newState = env.execute( env.structureToAction(agName, action.actionTerm) )
        historyS.add(newState)
        action.result = true
        actionExecuted(action)
    }
}