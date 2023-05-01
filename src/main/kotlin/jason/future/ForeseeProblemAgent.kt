package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Event
import jason.asSemantics.Intention
import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS
import java.util.concurrent.LinkedBlockingDeque

enum class ExplorationStrategy { NONE, ONE, LEVEL1, DFS, BFS }

/** agent that considers the future */
open class ForeseeProblemAgent : PreferenceAgent() {

    private var explorationStrategy : ExplorationStrategy = defaultStrategy()
    private val orderOptions = true // whether options are ordered before explored
    fun strategy() = explorationStrategy

    // search data structure
    val explorationQueue = LinkedBlockingDeque<FutureOption>()
    val visitedOptions = mutableSetOf< Pair<State,String> >() // to speed the search

    // result of the search (based on a good future found during search)
    val goodOptions = mutableMapOf< Intention, MutableMap<State,Option>>() // store good options found while verifying the future

    fun optionsCfParameter(options: MutableList<Option>) : List<Option> =
        if (orderOptions)
            super.sortedOptions(options, explorationStrategy == ExplorationStrategy.BFS || explorationStrategy == ExplorationStrategy.LEVEL1)
        else
            options

    fun userEnv() : MatrixCapable<*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*>

    fun envModel() : EnvironmentModel<State> =
        userEnv().getModel() as EnvironmentModel<State>

    /** returns true of the option should be explored */
    fun explore(s: State, o: Option) : Boolean {
        return !visitedOptions.contains( Pair( s, o.plan.label.functor))
    }

    fun addToExplore(fo: FutureOption) {
        //println("+${fo.arch.env.currentState()}/${fo.o.plan.label.functor} in    $visitedOption")
        if (visitedOptions.add( Pair(fo.arch.env.currentState(), fo.o.plan.label.functor))) {
            when (explorationStrategy) {
                ExplorationStrategy.BFS    -> explorationQueue.offerLast(fo)
                ExplorationStrategy.DFS    -> explorationQueue.offerFirst(fo)
                ExplorationStrategy.LEVEL1 -> explorationQueue.offerLast(fo)
                ExplorationStrategy.ONE    -> explorationQueue.offerLast(fo)
                else -> {}
            }
        }
    }

    fun curInt() = ts.c.selectedEvent.intention

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options) ?: return null

        if (curInt() == null || explorationStrategy == ExplorationStrategy.NONE) // we are considering options only for an intention
            return defaultOption

        setInstance(this) // for the GUI interface to change strategy

        // if I found a good option while checking futures... use it here
        val goodOpt = goodOptions[curInt()]?.get(envModel().currentState())
        if (goodOpt != null) {
            println("reusing option ${goodOpt.plan.label.functor} for ${envModel().currentState()}")
            return goodOpt
        }

        // simulates the future of options

        try {
            // clone agent, environment, options ... building FutureOptions to be added into exploration queue
            if (explorationStrategy == ExplorationStrategy.ONE) {
                addToExplore(prepareSimulation(defaultOption))
            } else {
                for (o in optionsCfParameter(options)) {
                    addToExplore(prepareSimulation(o))
                }
            }

            // explore future options to see their future
            var nbE = 0
            var fo = explorationQueue.poll()
            while (fo != null && nbE < 10000) { // TODO: add a parameter somewhere to define o max number os options to explore
                nbE++

                println("\nstarting simulation for goal ${fo.evt.trigger.literal}@${fo.arch.env.currentState()} with plan @${fo.o.plan.label.functor}, I have ${explorationQueue.size} options still. Depth=${fo.ag.depth()}")

                // run agent with event and option to be explored
                fo.evt.option = fo.o // set the option to be used for the new event (jason selects this option for the event, if set)
                fo.ag.ts.c.addEvent(fo.evt) // and add the event into the Jason queue
                fo.arch.run(fo.evt)

                if (!fo.arch.hasProblem()) {
                    println("found an option with a likely nice future! $nbE options tried. option=${envModel().currentState()}->${fo.ag.originalOption?.plan?.label?.functor}")
                    printPlan(fo)
                    storeGoodOptions(fo)
                    return fo.ag.originalOption
                }
                fo = explorationQueue.poll()
            }
            println("\nsorry, all options have an unpleasant future. aborting the intention! (tried $nbE options)\n")
            return null
        } finally {
            visitedOptions.clear()
            explorationQueue.clear()
        }
    }

    fun printPlan(fo: FutureOption) {
        var f : FutureOption = fo
        var s = ""
        while (f.previousFO != null) {
            s = "${f.state}->${f.o.plan.label.functor}, " + s
            f = f.previousFO!!
        }
        s += " ---- "
        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            s += "${h[i]}->${o[i].plan.label.functor}, "
        }
        println("    plan is $s")
    }

    fun storeGoodOptions(fo: FutureOption) {
        var f : FutureOption = fo
        goodOptions.putIfAbsent(curInt(), mutableMapOf())
        while (f.previousFO != null) {
            goodOptions[curInt()]?.put( f.state, f.o)
            f = f.previousFO!!
        }
        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            goodOptions[curInt()]?.put(h[i], o[i])
        }
    }

    fun prepareSimulation(opt: Option) : FutureOption {
        // clone agent model (based on this agent)
        /*val agArch = MatrixAgentArch(
            envModel().clone(),
            "${ts.agArch.agName}_matrix"
        )
        val agModel = MatrixAgent(this, opt)
        this.cloneInto(agArch, agModel)
        agModel.ts.setLogger(agArch)
        val agModel = MatrixAgent.buildAg( envModel(), this, this, opt)

        val fo = FutureOption(
            opt,
            agModel,
            agModel.myMatrixArch(),
            this.ts.c.selectedEvent.clone() as Event,
            null,
            envModel().currentState())
        agModel.myFO = fo
        return fo*/
        return MatrixAgent.buildAg(opt, envModel(), this, opt, this)
    }


    companion object {
        @Volatile
        private var instance: ForeseeProblemAgent? = null

        fun getInstance() = instance
        fun setInstance(a: ForeseeProblemAgent) { instance = a }
        fun defaultStrategy() =  ExplorationStrategy.BFS
        fun setStrategy(e: ExplorationStrategy) {
            instance?.explorationStrategy = e
            println("exploration set to "+e)
        }
    }
}

data class FutureOption(
    val evt: Event,     // event for which this FO was created
    val o: Option,      // option where this FO was created
    val state: State,   // state where this FO was created
    val ag: MatrixAgent, // agent that will handle/simulate this FO
    val arch: MatrixAgentArch, // and  its arch
    val previousFO: FutureOption? // FO that generated this one (to track back the root of exploration)
)