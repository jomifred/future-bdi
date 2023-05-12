package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Intention
import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

enum class ExplorationStrategy { NONE, ONE, LEVEL1, SOLVE_P, SOLVE_F, SOLVE_M }

fun Double.format(pre: Int, digits: Int) = "%${pre}.${digits}f".format(this)

/** agent that considers the future */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : PreferenceAgent() {

    init {
        getImplementedStrategies().add(ExplorationStrategy.LEVEL1)
        getImplementedStrategies().add(ExplorationStrategy.SOLVE_P)
        getImplementedStrategies().add(ExplorationStrategy.SOLVE_M)
    }

    //private val orderOptions = true // whether options are ordered before explored

    // search data structure
    private var explorationQueue  = PriorityBlockingQueue<FutureOption>()

    private val visitedOptions = mutableSetOf< Pair<State,String> >() // to speed the search
    protected val inQueueOptions = mutableMapOf< Pair<State,String>, Double> () // to speed the search: options and their evaluation/quality

    // result of the search (based on a good future found during search)
    private val goodOptions = mutableMapOf< Intention, MutableMap<State,Option>>() // store good options found while verifying the future

    private fun userEnv() : MatrixCapable<*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*>

    private fun envModel() : EnvironmentModel<State> =
        userEnv().getModel() as EnvironmentModel<State>

    /** returns true of the option should be explored (if not visited already) */
    fun explore(s: State, o: Option) : Boolean {
        return !visitedOptions.contains( Pair( s, o.plan.label.functor) )
    }

    open fun optionsCfParameter(options: MutableList<Option>) : List<Option> =  options

    open fun addToExplore(fo: FutureOption) {
        val currentFO = inQueueOptions.getOrDefault(fo.getPairId(), Double.MAX_VALUE)
        if (fo.eval() < currentFO) { // if the new option is better (or new), add to explore
            inQueueOptions[fo.getPairId()] = fo.eval()
            explorationQueue.add(fo)
        }
    }
    open fun getToExplore() : FutureOption? = explorationQueue.poll()

    fun curInt() : Intention = ts.c.selectedEvent.intention

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options) ?: return null

        if (ts.c.selectedEvent.intention == null // we are considering options only for an intention
            || options.size == 1 // nothing to chose
            || solveStrategy == ExplorationStrategy.NONE)
            return defaultOption

        // if I found a good option while checking futures... reuse it here
        val goodOpt = goodOptions[curInt()]?.get(envModel().currentState())
        if (goodOpt != null) {
            println("reusing option ${goodOpt.plan.label.functor} for ${envModel().currentState()}")
            return goodOpt
        }

        // simulates the future of options

        try {
            // clone agent, environment, options ... building FutureOptions to be added into exploration queue
            when (solveStrategy) {
                ExplorationStrategy.ONE -> addToExplore(prepareSimulation(defaultOption))

                ExplorationStrategy.SOLVE_P, ExplorationStrategy.LEVEL1, ExplorationStrategy.SOLVE_M -> {
                    for (o in optionsCfParameter(options)) {
                        addToExplore(prepareSimulation(o))
                    }
                }
                else -> {}
            }

            // explore future options to see their future
            var nbE = 0
            var fo = getToExplore()
            fo?.arch?.getAg()?.inSolveMPhase1 = true // so that all future options are produced for default option (not just first level)
            while (fo != null && nbE < 10000) { // TODO: add a parameter somewhere to define o max number os options to explore
                nbE++

                println("\nstarting simulation for goal ${fo.evt.trigger.literal}@${fo.arch.env.currentState()} with plan @${fo.opt.plan.label.functor}, I still have ${explorationQueue.size} options. Depth=${fo.depth}")
                visitedStates.add( fo.state ) // for GUI
                visitedOptions.add( fo.getPairId() )

                // run agent with event and option to be explored
                fo.evt.option = fo.opt // set the option to be used for the new event (jason selects this option for the event, if set)
                fo.ag.ts.c.addEvent(fo.evt) // and add the event into the Jason queue
                fo.ag.lastFO = fo
                fo.arch.run(fo.evt)

                if (!fo.arch.hasProblem()) {
                    println("found an option with a likely nice future! $nbE options tried. option=${envModel().currentState()}->${fo.ag.originalOption.plan?.label?.functor}, cost=${fo.cost}")
                    if (nbE > 1)
                        setMsg("explored $nbE options to find a nice future. cost=${fo.cost}, depth=${fo.depth}.")
                    val planStr = storeGoodOptions(fo)
                    println("    plan is $planStr")

                    return fo.ag.originalOption

                } else if (solveStrategy == ExplorationStrategy.SOLVE_M && nbE == 1) {
                    // I just tried the default option
                    /*val planStr = storeGoodOptions(fo)
                    println("    plan is $planStr")
                    for (c  in explorationQueue) {
                        println("   fo ${c.state} f=${c.eval().format(4,1)}=${c.cost.format(4,1)}+${c.heuristic.format(4,1)}. depth=${c.depth}")
                    }
                    println("    options=${explorationQueue.size}")*/
                    //break
                }
                fo = getToExplore() // continue to explore
            }
            println("\nsorry, all options have an unpleasant future. aborting the intention! (tried $nbE options)\n")
            setMsg("explored $nbE options and ... no future")
            return null
        } finally {
            visitedOptions.clear()
            inQueueOptions.clear()
            explorationQueue.clear()
        }
    }

    private fun storeGoodOptions(fo: FutureOption) : String {
        solution.clear() // used by the GUI
        var f = fo
        var planStr = ""

        goodOptions.putIfAbsent(curInt(), mutableMapOf())
        while (f.parent != null) {
            goodOptions[curInt()]?.put( f.state, f.opt)
            solution.add(0, f.state)
            planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr
            f = f.parent!!
        }
        planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr

        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            goodOptions[curInt()]?.put(h[i], o[i])
            if (i>0) {
                planStr += "${h[i]}->${o[i].plan.label.functor}, "
                solution.add( h[i] )
            }
        }
        return planStr
    }

    private fun prepareSimulation(opt: Option) : FutureOption {
        return MatrixAgent.buildAg(opt, envModel(), this, opt, this,
            0.0,
            null)
    }


    companion object {
        private var msg: String = ""
        private var solveStrategy = ExplorationStrategy.ONE
        private var implementedStrategies = mutableSetOf(
            ExplorationStrategy.NONE,
            ExplorationStrategy.ONE
        )

        private val visitedStates = ConcurrentHashMap.newKeySet<State>()
        private var solution      : MutableList<State> = mutableListOf()

        fun getImplementedStrategies() = implementedStrategies

        fun getVisited() : Set<State> = visitedStates
        fun clearVisited() { visitedStates.clear() }
        fun getSolution() = solution
        fun strategy() = solveStrategy
        fun setStrategy(e: ExplorationStrategy) {
            solveStrategy = e
            println("exploration set to $e")
            msg = ""
        }

        fun setMsg(s: String) { msg = s }
        fun getMsg() = msg
    }
}
