package jason.future

import jason.architecture.AgArch
import jason.asSemantics.ActionExec
import jason.asSemantics.Event
import jason.asSyntax.Literal

/** agent that run in the "Matrix" (simulated world) */
class MatrixAgentArch (
    val env    : EnvironmentModel<State>,
    private val agName : String
) : AgArch() {

    private val history = mutableListOf<State>()
    private var hasLoop = false
    private var alreadyVisited = false // if I've got to a state explored by other options

    override fun getAgName(): String {
        return agName
    }

    override fun perceive(): MutableCollection<Literal> {
        //println("        matrix perception: ${env.agPerception(agName)}")
        return env.agPerception(agName)
    }

    override fun act(action: ActionExec) {
        //println("        matrix action: ${action.actionTerm}")
        val newState = env.execute( env.structureToAction(action.actionTerm) )

        //val agent = ts.ag as ForeseeProblemAgent
        //alreadyVisited = agent.originalAgent?.visited?.unzip()?.first?.contains(newState)?:false // TODO: optimise this
        //alreadyVisited = agent.originalAgent?.visitedStates?.contains(newState)?:false
        //alreadyVisited = !(agent.originalAgent?.visited?.add(newState)?:true)
        //println("        visited ${agent.originalAgent?.visited}")
        hasLoop = history.contains(newState)
        history.add(newState)
        action.result = true
        actionExecuted(action)
    }

    /** returns true if the simulated history has problem */
    fun hasProblem() = hasLoop || alreadyVisited

    fun run(evt: Event) {
        val intention = evt.intention
        var rcCounter = 0
        while (!intention.isFinished && !hasProblem() && rcCounter < 100) { // TODO: give a way to set this number
            rcCounter++

            ts.sense()
            ts.deliberate()
            ts.act()
            //println("    $rcCounter: act = ${ts.c.action?.actionTerm}.  int size=${intention.size()}. se=${ts.c.selectedEvent?.trigger}. ints=${ts.c.runningIntentions.size}")
            //println("    history: ${history}")
        }
        println("    simulation finished in $rcCounter steps. intention finished=${intention.isFinished}. problem=${hasProblem()}. visited=$alreadyVisited")
    }
}