package jason.future

import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.PriorityBlockingQueue

enum class ExplorationStrategy { NONE, ONE, SOLVE_P, SOLVE_M, SOLVE_F }

/** search for a good option for the agent */
open class Search (
    val mainAg  : ForeseeProblemAgent,
    val conds   : StopConditions,
    val strategy: ExplorationStrategy,
    val envModel: EnvironmentModel<State, Action>) {

    // search data structure
    private var explorationQueue  = PriorityBlockingQueue<FutureOption>()

    private val visitedOptions = mutableSetOf< Pair<State,String> >() // to speed the search
    private val inQueueOptions = mutableMapOf< Pair<State,String>, Double> () // to speed the search: options and their evaluation/quality

    /** select an option to explore */
    open fun select() : FutureOption? = explorationQueue.poll()

    /** add an option to be explored */
    open fun expand(fo: FutureOption) {
        val currentFO = inQueueOptions.getOrDefault(fo.getPairId(), Double.MAX_VALUE)
        if (fo.eval() < currentFO) { // if the new option is better (or new), add to explore
            inQueueOptions[fo.getPairId()] = fo.eval()
            explorationQueue.add(fo)
        }
    }

    /** returns true of the option should be explored (if not visited already) */
    fun shouldExplore(s: State, o: Option) : Boolean {
        return !visitedOptions.contains( Pair( s, o.plan.label.functor) )
    }

    //open fun optionsCfParameter(options: MutableList<Option>) : List<Option> =  options


    lateinit var matrix : MatrixRunner

    fun run() : FutureOption? {
        try {
            // explore future options to see their future
            var nbE = 0
            var fo = select()
            fo?.ag?.inZone1 = true // current options + those in the future of default options are in zone1
            var visited = 0
            var defaultPlan: Set<State>? = null // used for stats (compute how many stes are in the ag policy)
            while (fo != null && nbE < 3000) { // TODO: add a parameter somewhere to define o max number os options to explore
                nbE++

                println("\nstarting simulation $nbE for goal ${fo.opt.evt.trigger.literal}@${fo.states()} with plan @${fo.opt.plan.label.functor}, I still have ${explorationQueue.size} options. Depth=${fo.depth}")
                matrix = rollout(fo)

                visited += fo.planSize()
                ForeseeProblemAgent.data.nbVisitedStates += visited

                if (nbE == 1)
                    defaultPlan = fo.states().first.toSet()

                if (matrix.success()) {
                    println("found an option with a likely nice future! ${"%.8f".format(matrix.certainty)} of certainty. $nbE options tried. option=${envModel.currentState()}->${fo.ag.originalOption.plan?.label?.functor}, cost=${fo.cost}")
                    if (nbE > 1)
                        ForeseeProblemAgent.setMsg("explored $nbE options to find a nice future. depth=${fo.planSize()} visited=${visited}.")
                    //val planStr = mainAg.storeGoodOptions(fo)
                    println("    plan is ${fo.allActions()}")
                    storeStats(fo, nbE, visited, defaultPlan?:setOf<State>())

                    return fo //.ag.originalOption
                }

                fo = select() // continue to explore
            }
            storeStats(null, nbE, visited, defaultPlan?:setOf<State>())
            println("\nsorry, all options have an unpleasant future!\n")
            ForeseeProblemAgent.setMsg("explored $nbE options and ... no future")
            return null
        } finally {
            visitedOptions.clear()
            inQueueOptions.clear()
            explorationQueue.clear()
        }
    }

    fun init(options: List<Option>) {
        for (o in options) {
            if (shouldExplore(envModel.currentState(), o))
                expand(prepareSimulation(o))
        }
    }

    fun prepareSimulation(opt: Option) : FutureOption {
        return FutureOption.build(opt, envModel, mainAg, opt, mainAg,
            0.0,
            null, 1.0, this,
            RunLocalMAS.getRunner().ags)
    }

    fun rollout(fo: FutureOption) : MatrixRunner {
        if (envModel.hasGUI()) ForeseeProblemAgent.visitedStates.add( fo.state ) // for GUI
        visitedOptions.add( fo.getPairId() )

        // run agent with event and option to be explored
        fo.opt.evt.option = fo.opt // set the option to be used for the new event (jason selects this option for the event, if set)
        fo.ag.ts.c.addEvent(fo.opt.evt) // and add the event into the Jason queue
        fo.ag.lastFO = fo
        val m = MatrixRunner(fo.agArch().env, conds, fo)
        m.addAg( fo.agArch() )
        fo.otherAgs().values.forEach{ m.addAg( it ) }
        m.run()

        println("    simulation finished in ${m.steps} steps and certainty ${"%.8f".format(m.certainty)}. intention finished=${m.success()}. problem=${m.failure()}.")
        println("    history=${m.historyS}")
        return m
    }

    fun storeStats(fo: FutureOption?, nbOptions: Int, statesVisited: Int, defaultPlan: Set<State>) {
        if (statesVisited <= 0)
            return
        try {
            BufferedWriter(FileWriter("stats.txt", true)).use { out ->
                val planSize = fo?.planSize()?:0
                val foSt = fo?.states()
                val foPlan = foSt?.first?:emptyList()
                val foPrePlan = foPlan.subList(0,foSt?.second?:0)
                val commonStates =
                    if (planSize == 1 || fo == null)
                        0
                    else
                        defaultPlan.intersect(foPrePlan).size
                val inPolicy =
                    if (fo == null)
                        0
                    else
                        commonStates + (fo.planSize() - foSt!!.second)
                val inPolicyP = if (planSize>0) 100*inPolicy/planSize else 0
                //println(defaultPlan)
                //println(foPlan.toString() + " " + foSt?.second)
                //println("preFo="+foPrePlan)
                //println("df-fo="+foPlan.minus(defaultPlan))
                //println("$commonStates $inPolicy ${foSt?.second} ${foPlan?.size} ${fo?.planSize()}")
                //println("| ${fo?.arch?.env?.id()} | ${ForeseeProblemAgent.strategy()} | ${if (fo==null) "no" else "yes" } | $nbOptions | $statesVisited | ${if (fo==null) "--" else planSize} | $inPolicy (${inPolicyP}%)")
                out.appendLine("| ${envModel.id()} | ${ForeseeProblemAgent.strategy()} | ${if (fo==null) "no" else "yes" } | $nbOptions | $statesVisited | ${if (fo==null) "--" else planSize} | $inPolicy (${inPolicyP}%)")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

