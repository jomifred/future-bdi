package jason.future

import jason.architecture.AgArch
import jason.asSemantics.ActionExec
import jason.asSemantics.Event
import jason.asSemantics.Intention
import jason.asSyntax.Literal

/** agent that run in the "Matrix" (simulated world) */
class MatrixAgentArch (
    private val env : EnvironmentModel<State>,
    private val agName : String
) : AgArch() {

    private val history = mutableListOf<State>()

    override fun getAgName(): String {
        return agName
    }

    override fun perceive(): MutableCollection<Literal> {
        println("        matrix perception: ${env.agPerception(agName)}")
        return env.agPerception(agName)
    }

    override fun act(action: ActionExec) {
        println("        matrix action: ${action.actionTerm}")
        history.add(
            env.execute( env.structureToAction(action.actionTerm) )
        )
        action.result = true
        actionExecuted(action)
    }

//    override fun actionExecuted(act: ActionExec?) {
//        println("executed: ${act?.actionTerm}")
//        super.actionExecuted(act)
//    }

    fun run(evt: Event) {
        val intention = evt.intention
        var rcCounter = 0
        var a : Literal? = null
        while (!intention.isFinished && rcCounter < 10) { // TODO: give a way to set this number
            rcCounter++
            //println("looking $rcCounter steps ahead")

            ts.sense()
            ts.deliberate()
            ts.act()
            println("    $rcCounter: act = ${ts.c.action?.actionTerm}.  int size=${intention.size()}. se=${ts.c.selectedEvent?.trigger}. ints=${ts.c.runningIntentions.size}")
            println("    history: ${history}")
        }
        println("intention finished, in $rcCounter steps")
    }
}