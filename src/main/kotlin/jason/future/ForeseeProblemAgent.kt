package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Intention
import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.PriorityBlockingQueue

enum class ExplorationStrategy { NONE, ONE, LEVEL1, SOLVE_P, SOLVE_F }

/** agent that considers the future */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : PreferenceAgent() {

    //private val orderOptions = true // whether options are ordered before explored

    // search data structure
    private var explorationQueue  = PriorityBlockingQueue<FutureOption>()
    private val explorationQueueF = LinkedBlockingDeque<FutureOption>()

    private val visitedOptions = mutableSetOf< Pair<State,String> >() // to speed the search
    private val inQueueOptions = mutableMapOf< Pair<State,String>, Double> () // to speed the search: options and their evaluation/quality

    // result of the search (based on a good future found during search)
    private val goodOptions = mutableMapOf< Intention, MutableMap<State,Option>>() // store good options found while verifying the future

    private fun userEnv() : MatrixCapable<*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*>

    private fun envModel() : EnvironmentModel<State> =
        userEnv().getModel() as EnvironmentModel<State>

    /** returns true of the option should be explored (if not visited already) */
    fun explore(s: State, o: Option) : Boolean {
        return !visitedOptions.contains( Pair( s, o.plan.label.functor) )
    }

    fun optionsCfParameter(options: MutableList<Option>) : List<Option> =
        if (solveStrategy == ExplorationStrategy.SOLVE_F)
            super.sortedOptions(options, false)
        else
            options

    fun addToExplore(fo: FutureOption) {
        val currentFO = inQueueOptions.getOrDefault(fo.getPairId(), Double.MAX_VALUE)
        if (fo.eval() < currentFO) { // if the new option is better (or new), add to explore
            inQueueOptions[fo.getPairId()] = fo.eval()
            when (solveStrategy) {
                ExplorationStrategy.SOLVE_F -> explorationQueueF.offerFirst(fo)
                else -> { explorationQueue.add(fo) }
            }
        }
    }
    private fun getToExplore() : FutureOption? =
        when (solveStrategy) {
            ExplorationStrategy.SOLVE_F -> explorationQueueF.poll()
            else -> { explorationQueue.poll() }
        }

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
            if (solveStrategy == ExplorationStrategy.ONE) {
                addToExplore(prepareSimulation(defaultOption))
            } else {
                for (o in optionsCfParameter(options)) {
                    addToExplore(prepareSimulation(o))
                }
            }

            // explore future options to see their future
            var nbE = 0
            var fo = getToExplore()
            while (fo != null && nbE < 10000) { // TODO: add a parameter somewhere to define o max number os options to explore
                nbE++

                println("\nstarting simulation for goal ${fo.evt.trigger.literal}@${fo.arch.env.currentState()} with plan @${fo.opt.plan.label.functor}, I still have ${explorationQueue.size} options. Depth=${fo.depth}")
                visitedStates.add( fo.state ) // for GUI
                visitedOptions.add( fo.getPairId() )

                // run agent with event and option to be explored
                fo.evt.option = fo.opt // set the option to be used for the new event (jason selects this option for the event, if set)
                fo.ag.ts.c.addEvent(fo.evt) // and add the event into the Jason queue
                fo.arch.run(fo.evt)

                if (!fo.arch.hasProblem()) {
                    println("found an option with a likely nice future! $nbE options tried. option=${envModel().currentState()}->${fo.ag.originalOption.plan?.label?.functor}, cost=${fo.cost}")
                    if (nbE > 1)
                        setMsg("explored $nbE options to find a nice future. cost=${fo.cost}, depth=${fo.depth}.")
                    printPlan(fo)

                    solution = storeGoodOptions(fo)
                    return fo.ag.originalOption
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
            explorationQueueF.clear()
        }
    }

    private fun printPlan(fo: FutureOption) {
        var f : FutureOption = fo
        var s = ""
        while (f.parent != null) {
            s = "${f.state}->${f.opt.plan.label.functor}, " + s
            f = f.parent!!
        }
        s += " ---- "
        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            s += "${h[i]}->${o[i].plan.label.functor}, "
        }
        println("    plan is $s")
    }

    private fun storeGoodOptions(fo: FutureOption) : List<State> {
        val path = mutableListOf<State>()
        var f : FutureOption = fo
        goodOptions.putIfAbsent(curInt(), mutableMapOf())
        while (f.parent != null) {
            goodOptions[curInt()]?.put( f.state, f.opt)
            path.add(0, f.state)
            f = f.parent!!
        }
        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            goodOptions[curInt()]?.put(h[i], o[i])
            if (i>0) path.add( h[i])
        }
        return path
    }

    private fun prepareSimulation(opt: Option) : FutureOption {
        return MatrixAgent.buildAg(opt, envModel(), this, opt, this)
    }


    companion object {
        private var msg: String = ""
        private var solveStrategy = ExplorationStrategy.ONE

        private val visitedStates = ConcurrentHashMap.newKeySet<State>()
        private var solution      : List<State> = mutableListOf()

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
