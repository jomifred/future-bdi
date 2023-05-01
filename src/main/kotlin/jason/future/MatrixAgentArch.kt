package jason.future

import jason.architecture.AgArch
import jason.asSemantics.ActionExec
import jason.asSemantics.Event
import jason.asSemantics.Option
import jason.asSyntax.Literal

/** agent that run in the "Matrix" (simulated world) */
class MatrixAgentArch (
    val env    : EnvironmentModel<State>,
    private val agName : String
) : AgArch() {

    val historyS = mutableListOf<State>()
    val historyO = mutableListOf<Option>()

    private var lastOpt : Option? = null

    private var hasLoop = false

    override fun getAgName(): String {
        return agName
    }

    override fun perceive(): MutableCollection<Literal> {
        //println("        matrix perception: ${env.agPerception(agName)}")
        return env.agPerception(agName)
    }

    override fun act(action: ActionExec) {
        //println("        matrix action: ${action.actionTerm}")
        if (lastOpt != null)
            historyO.add(lastOpt!!)

        val newState = env.execute( env.structureToAction(action.actionTerm) )
        hasLoop = historyS.contains(newState)
        historyS.add(newState)
        action.result = true
        actionExecuted(action)
    }

    /** returns true if the simulated history has problem */
    fun hasProblem() = hasLoop //|| alreadyVisited

    fun run(evt: Event) {
        historyS.add( env.currentState() )
        val intention = evt.intention
        var rcCounter = 0
        while (!intention.isFinished && !hasProblem() && rcCounter < 5000) { // TODO: give a way to set this number
            rcCounter++

            ts.sense()
            ts.deliberate()
            if (ts.c.selectedOption != null) lastOpt = ts.c.selectedOption
            ts.act()
            //println("    $rcCounter: act = ${ts.c.action?.actionTerm}.  int size=${intention.size()}. se=${ts.c.selectedEvent?.trigger}. ints=${ts.c.runningIntentions.size}")
            //println("    history: ${history}")
        }
        println("    simulation finished in $rcCounter reasoning cycles. intention finished=${intention.isFinished}. problem=${hasProblem()}.")
        println("    history=$historyS")
    }
}