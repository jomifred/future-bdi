package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.NoOptionException
import jason.asSemantics.Option
import jason.asSyntax.ASSyntax
import jason.infra.local.RunLocalMAS
import jason.mas2j.AgentParameters
import jason.runtime.Settings.PROJECT_PARAMETER
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

//fun Double.format(pre: Int, digits: Int) = "%${pre}.${digits}f".format(this)

/** agent that considers the future */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : PreferenceAgent(), StopConditions {

    // result of the search (based on a good future found during search)
    //private val goodOptions = mutableMapOf< Intention, MutableMap<State,Option>>() // store good options found while verifying the future

    /** required certainty to progress running matrix */
    var rCertainty = 0.0

    override fun initAg() {
        super.initAg()
        try {
            val agC = (ts.settings.userParameters[PROJECT_PARAMETER] as AgentParameters).agClass
            if (agC.parameters.isNotEmpty()) {
                var dStrategy = agC.getParameter(0)
                dStrategy = dStrategy.substring(1, dStrategy.length - 1)
                detectionStrategy = ExplorationStrategy.valueOf(dStrategy)
            }

            val conf = Properties()
            try {
                conf.load(FileReader("params.properties"))
                rCertainty = conf.getOrDefault("requiredCertainty", rCertainty).toString().toDouble()

                data.strategy = ExplorationStrategy.valueOf(
                    conf.getOrDefault("recover_strategy", "NONE").toString())
                addBel(ASSyntax.parseLiteral("r_strategy(\"${data.strategy}\")"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            data.requiredCertainty = rCertainty

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun userEnv() : MatrixCapable<*,*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*,*>

    fun envModel() : EnvironmentModel<State, Action>? {
        try {
            return userEnv().getModel() as EnvironmentModel<State, Action>
        } catch (e: NullPointerException) {
            //e.printStackTrace()
            return null
        }
    }


    //fun curInt() : Intention = ts.c.selectedEvent.intention

    // whether matrix should stop due to a problem
    //open fun hasProblem(history: List<State>, hasLoop : Boolean) = hasLoop
    //open fun hasProblem(history: List<State>, hasLoop : Boolean) = history.size > 50

    //override fun success(history: List<State>, steps: Int): Boolean =  curInt().isFinished

    @Throws(NoOptionException::class)
    override fun selectOption(options: MutableList<Option>): Option? {
        if (envModel()?.hasGUI() == true) solution.clear() // GUI
        clearVisited()

        val defaultOption = super.selectOption(options) ?: return null
        //println("In select option for ${defaultOption?.evt?.trigger}")

        if (ts.c.selectedEvent.intention == null // we are considering options only for an intention
            || detectionStrategy == ExplorationStrategy.NONE
            || defaultOption.evt.trigger.isFailureGoal) // do not use matrix for failure goals
            return defaultOption

        // if I found a good option while checking futures... reuse it here
        /*val goodOpt = goodOptions[curInt()]?.get(envModel().currentState())
        if (goodOpt != null) {
            println("reusing option ${goodOpt.plan.label.functor} for ${envModel().currentState()}")
            return goodOpt
        }*/

        // simulates the future of options
        val search = Search(this, this, detectionStrategy, envModel()!!)
        //val search = Search(this, ExplorationStrategy.ONE, envModel())
        if (detectionStrategy == ExplorationStrategy.ONE)
            search.init( listOf<Option>(defaultOption) )
        else
            search.init( options )
        val rFO = search.run()

        if (rFO != null) { // a good plan was found
            if (envModel()?.hasGUI() == true) solution.addAll(rFO.states().first)  // show solution in GUI
            return rFO.ag.originalOption
        }

        // no future ... but returns default options
        if (search.matrix.failure())
            throw NoOptionException("there will be a failure to handle ${defaultOption.evt.trigger} in the future! (states ahead: ${search.matrix.historyS})", ASSyntax.createAtom("no_future"))
        if (search.matrix.stop())
            if (envModel()?.hasGUI() == true) solution.addAll(search.matrix.fo.states().first) // GUI
        return defaultOption
    }

    // stop condition for matrix running
    override fun stop(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop : Boolean, certainty: Double) =
        steps > 5000 || certainty < rCertainty

    // TODO: store action performed while in matrix
    //val planBodyFound = StringBuilder()

    /*
    var latestFO : FutureOption? = null

    fun storeGoodOptions(fo: FutureOption) : String {
        latestFO = fo

        solution.clear() // used by the GUI
        var f = fo
        var planStr = ""
        //planBodyFound.clear()

        //goodOptions.putIfAbsent(curInt(), mutableMapOf())
        while (f.parent != null) {
            //goodOptions[curInt()]?.put( f.state, f.opt)
            solution.add(0, f.state)
            planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr
            //planBodyFound.insert(0,"${f.opt.plan.label.functor}; ")
            f = f.parent!!
        }
        planStr = "${f.state}-->${f.opt.plan.label.functor}, " + planStr
        //planBodyFound.insert(0,"${f.opt.plan.label.functor}; ")

        val h = fo.arch.historyS
        val o = fo.arch.historyO
        for (i in 0 until minOf(h.size,o.size)) {
            //goodOptions[curInt()]?.put(h[i], o[i])
            if (i>0) {
                planStr += "${h[i]}->${o[i].plan.label.functor}, "
                //planBodyFound.append("${o[i].plan.label.functor}; ")
                solution.add( h[i] )
            }
        }
        return planStr
    }
    */


    companion object {
        private var msg: String = ""
        private var detectionStrategy = ExplorationStrategy.ONE
        private var solution      : MutableList<State> = mutableListOf()
        val visitedStates = ConcurrentHashMap.newKeySet<State>()
        val data = ExperimentData()

        fun getImplementedStrategies() = ExplorationStrategy.values()

        fun getVisited() : Set<State> = visitedStates
        fun clearVisited() { visitedStates.clear() }
        fun getSolution() = solution
        fun strategy() = detectionStrategy
        fun setStrategy(e: ExplorationStrategy) {
            detectionStrategy = e
            println("exploration set to $e")
            msg = ""
        }

        fun setMsg(s: String) { msg = s }
        fun getMsg() = msg
    }
}

class ExperimentData {
    var gamma = 0.0
    var pChange = 0.0
    var scenario = "none"
    var nbPlanFor = 0
    var nbMatrices = 0
    var nbVisitedStates = 0
    var strategy = ExplorationStrategy.NONE
    var requiredCertainty = 0.0
    var nbActions = 0
    var startT : Long
    init {
        startT = System.currentTimeMillis()
    }
    fun storeStats() {
        try {
            val newf = ! File("stats.csv").exists()
            BufferedWriter(FileWriter("stats.csv", true)).use { out ->
                if (newf)
                    out.appendLine("scenario, pChange, gamma, recovery_strategy, required_certainty, build_plans, matrices, visited_states, actions, time")
                out.appendLine("$scenario, $pChange, $gamma, $strategy, $requiredCertainty, $nbPlanFor, $nbMatrices, $nbVisitedStates, $nbActions, ${System.currentTimeMillis()-startT}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
