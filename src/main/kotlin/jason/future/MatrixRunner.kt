package jason.future

import jason.asSemantics.Intention

/** Executes the matrix (simulated world) */
class MatrixRunner (
    val env    : EnvironmentModel<State>,
    val mainAg : ForeseeProblemAgent, // the agent that is interested in this matrix
    val intention: Intention // the target intention of this matrix
)  {
    val historyS = mutableListOf<State>()
    private var hasLoop = false

    // the list of agents in this matrix
    val ags = mutableListOf<MatrixAgentArch>()
    fun addAg(ag: MatrixAgentArch) { ags.add(ag) }

    fun hasProblem() = mainAg.hasProblem(historyS, hasLoop)

    var rcCounter = 0

    fun run() : List<State> {
        historyS.add( env.currentState() )
        while (!intention.isFinished && !hasProblem() && rcCounter < 5000) { // TODO: give a way to set this number
            rcCounter++

            var someAct = false;
            // run one step of each agent
            for (ag in ags) {
                ag.ts.sense()
                ag.ts.deliberate()
                //if (ag.ts.c.selectedOption != null) ag.lastOpt = ag.ts.c.selectedOption
                ag.ts.act()
                someAct = someAct || ag.ts.c.action != null
            }
            if (someAct) {
                val newState = env.currentState()
                hasLoop = historyS.contains(newState)
                historyS.add(newState)
            }

            //println("    $rcCounter: act = ${ts.c.action?.actionTerm}.  int size=${intention.size()}. se=${ts.c.selectedEvent?.trigger}. ints=${ts.c.runningIntentions.size}")
            //println("    history: ${history}")
        }
        println("    simulation finished in $rcCounter reasoning cycles. intention finished=${intention.isFinished}. problem=${hasProblem()}.")
        println("    history=$historyS")
        return historyS
    }
}
