package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Intention
import jason.asSemantics.NoOptionException
import jason.asSemantics.Option
import jason.asSyntax.ASSyntax
import jason.infra.local.RunLocalMAS
import jason.mas2j.AgentParameters
import jason.runtime.Settings.PROJECT_PARAMETER
import java.io.*
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

//fun Double.format(pre: Int, digits: Int) = "%${pre}.${digits}f".format(this)

/** agent that considers the future */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : PreferenceAgent() {

    // result of the search (based on a good future found during search)
    private val goodOptions = mutableMapOf< Intention, MutableMap<State,Option>>() // store good options found while verifying the future

    override fun initAg() {
        super.initAg()
        try {
            val agC = (ts.settings.userParameters[PROJECT_PARAMETER] as AgentParameters).agClass
            if (agC.parameters.isNotEmpty()) {
                var sStrategy = agC.getParameter(0)
                sStrategy = sStrategy.substring(1, sStrategy.length - 1)
                solveStrategy = ExplorationStrategy.valueOf(sStrategy)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun userEnv() : MatrixCapable<*,*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*,*>

    fun envModel() : EnvironmentModel<State, Action> =
        userEnv().getModel() as EnvironmentModel<State, Action>

    fun curInt() : Intention = ts.c.selectedEvent.intention

    // whether matrix should stop due to a problem
    open fun hasProblem(history: List<State>, hasLoop : Boolean) = hasLoop
    //open fun hasProblem(history: List<State>, hasLoop : Boolean) = history.size > 50


    @Throws(NoOptionException::class)
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
        val search = Search(this, solveStrategy, envModel())
        //val search = Search(this, ExplorationStrategy.ONE, envModel())
        search.init(defaultOption, options)
        val opt = search.run()
        if (opt == null) {
            throw NoOptionException("there will be a failure to handle ${curInt().peek()?.trigger?:"this intention"} in the future! (states ahead: ${search.matrix.historyS})", ASSyntax.createAtom("no_future"))
        }
        return opt
    }

    // TODO: store action performed while in matrix
    val planBodyFound = StringBuilder()
    var latestFO : FutureOption? = null

    fun storeGoodOptions(fo: FutureOption) : String {
        latestFO = fo

        solution.clear() // used by the GUI
        var f = fo
        var planStr = ""
        planBodyFound.clear()

        //goodOptions.putIfAbsent(curInt(), mutableMapOf())
        while (f.parent != null) {
            //goodOptions[curInt()]?.put( f.state, f.opt)
            solution.add(0, f.state)
            planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr
            planBodyFound.insert(0,"${f.opt.plan.label.functor}; ")
            f = f.parent!!
        }
        planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr
        planBodyFound.insert(0,"${f.opt.plan.label.functor}; ")

        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            //goodOptions[curInt()]?.put(h[i], o[i])
            if (i>0) {
                planStr += "${h[i]}->${o[i].plan.label.functor}, "
                planBodyFound.append("${o[i].plan.label.functor}; ")
                solution.add( h[i] )
            }
        }
        return planStr
    }


    companion object {
        private var msg: String = ""
        private var solveStrategy = ExplorationStrategy.ONE
        private var solution      : MutableList<State> = mutableListOf()
        val visitedStates = ConcurrentHashMap.newKeySet<State>()

        fun getImplementedStrategies() = ExplorationStrategy.values()

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
