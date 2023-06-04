package jason.future

import jason.architecture.AgArch
import jason.asSemantics.ActionExec
import jason.asSemantics.Option
import jason.asSyntax.Literal

/** agent that run in the "Matrix" (simulated world) */
class MatrixAgentArch (
    val env : EnvironmentModel<State, Action>,
    private val agName : String
) : AgArch() {

    val historyS = mutableListOf<State>()
    val historyO = mutableListOf<Option>()

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
        historyO.add(ts.c.selectedOption)

        val newState = env.execute( env.structureToAction(agName, action.actionTerm) )
        historyS.add(newState)
        action.result = true
        actionExecuted(action)
    }
}