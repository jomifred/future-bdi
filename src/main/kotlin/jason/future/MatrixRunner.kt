package jason.future

/** Executes the matrix (simulated world) */
open class MatrixRunner (
    val env    : EnvironmentModel<State, Action>,
    private val conds  : StopConditions,
    val fo     : FutureOption // future option that started this Matrix
)  {
    //protected var logger = Logger.getLogger(MatrixRunner::class.java.name)

    val historyS = mutableListOf<State>()
    private var hasLoop = false

    // the list of agents in this matrix
    private val ags = mutableListOf<MatrixAgArch>()

    fun addAg(ag: MatrixAgArch) { ags.add(ag) }

    fun success() = conds.success(historyS, steps, fo.intention())

    fun failure() = conds.failure(historyS, steps, stepsWithoutAct, hasLoop, ags)

    fun stop() = conds.stop(historyS, steps, stepsWithoutAct, hasLoop, certainty)

    private var steps = 0
    private var stepsWithoutAct = 0

    var certainty = fo.certainty

    fun run() : List<State> {
        ForeseeProblemAgent.data.addNbMatrices() // for stats

        historyS.add( env.currentState() )
        while (!stop() && !success() && failure() == null) {
            steps++

            // run one step of each agent (percept/deliberate), so all see the same state
            for (ag in ags) {
                //println("Running one step for ${ag.agName}")
                ag.ts.sense()
                //println("${ag.agName} events = ${ag.ts.c.events}")
                ag.ts.deliberate()
            }

            var someAct = false
            // run one act step of each agent
            for (ag in ags) {
                //println("performing action for ${ag.agName}")
                ag.ts.act()
                someAct = someAct || ag.ts.c.action != null
            }
            stepsWithoutAct++
            if (someAct) {
                stepsWithoutAct = 0
                certainty *= env.gamma()
                val newState = env.currentState()
                hasLoop = historyS.contains(newState)
                historyS.add(newState)
            }

            //println("    $rcCounter: act = ${ts.c.action?.actionTerm}.  int size=${intention.size()}. se=${ts.c.selectedEvent?.trigger}. ints=${ts.c.runningIntentions.size}")
            //println("    history: ${history}")
        }
        //println("    simulation finished in $rcCounter reasoning cycles. intention finished=${intention.isFinished}. problem=${hasProblem()}.")
        //println("    history=$historyS")
        //logger.info("ended by (stop/success/failure/intention finished) ${stop()}/${success()}/${failure()}/${fo.intention().isFinished}}")
        return historyS
    }
}
