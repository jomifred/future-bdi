package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.NoOptionException
import jason.asSemantics.Option
import jason.asSyntax.ASSyntax
import jason.infra.local.RunLocalMAS
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *  agent that considers the future
 *  and problems like loops in the behaviour/goal not achieved
 */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : PreferenceAgent(), StopConditions {

    /** required certainty to progress running matrix */
    private var rCertainty = 0.0

    override fun initAg() {
        super.initAg()
        try {
            val conf = Properties()
            try {
                conf.load(FileReader("params.properties"))
                rCertainty = conf.getOrDefault("requiredCertainty", rCertainty).toString().toDouble()
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
            return null
        }
    }

    @Throws(NoOptionException::class)
    override fun selectOption(options: MutableList<Option>): Option? {
        if (envModel()?.hasGUI() == true) solution.clear() // GUI
        clearVisited()

        val defaultOption = super.selectOption(options) ?: return null
        //println("In select option for ${defaultOption?.evt?.trigger}")

        if (ts.c.selectedEvent.intention == null // we are considering options only for an intention
            || data.strategy == ExplorationStrategy.NONE
            || defaultOption.evt.trigger.isFailureGoal) // do not use matrix for failure goals
            return defaultOption

        // simulates the future of options
        val search = Search(this, this, ExplorationStrategy.ONE, envModel()!!)
        search.init( listOf<Option>(defaultOption) )
        search.run()

        val failure = search.matrix.failure()
        if (failure != null) {
            val msg = "failure foreseen for handling ${defaultOption.evt.trigger} in the future! (states ahead: ${search.matrix.historyS})"
            logger.info("$msg -- $failure")
            throw NoOptionException(msg, ASSyntax.createLiteral("future_issue", failure)
            )
        }

        if (envModel()?.hasGUI() == true) solution.addAll(search.matrix.fo.states().first) // GUI
        return defaultOption
    }

    // stop condition for matrix running
    override fun stop(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop : Boolean, certainty: Double) =
        steps > 5000 || certainty < rCertainty

    companion object {
        private var msg: String = ""
        //private var recoverStrategy = ExplorationStrategy.SOLVE_M
        private var solution      : MutableList<State> = mutableListOf()
        val visitedStates = ConcurrentHashMap.newKeySet<State>()
        val data = ExperimentData()

        fun getImplementedStrategies() = ExplorationStrategy.values()

        fun getVisited() : Set<State> = visitedStates
        fun clearVisited() { visitedStates.clear() }
        fun getSolution() = solution
        /*fun strategy() = recoverStrategy
        fun setStrategy(e: ExplorationStrategy) {
            recoverStrategy = e
            println("exploration set to $e")
            msg = ""
        }*/

        fun setMsg(s: String) { msg = s }
        fun getMsg() = msg
    }
}

class ExperimentData {
    var gamma = 0.0
    var pChange = 0.0
    var scenario = "none"
    var nbPlanFor = 0
    private var nbMatrices = 0
    var nbVisitedStates = 0
    var strategy = ExplorationStrategy.NONE
    var requiredCertainty = 0.0
    var nbActions = 0
    var actionsCost = 0.0
    var startT : Long
    init {
        startT = System.currentTimeMillis()
    }

    fun addNbMatrices() {
        nbMatrices++
        /*if (nbMatrices > 5000) {
            System.exit(0)
        }*/
    }

    fun storeStats(timeout: Boolean) {
        try {
            val toS = if (timeout) "timeout" else "ontime"
            val newf = ! File("stats.csv").exists()
            BufferedWriter(FileWriter("stats.csv", true)).use { out ->
                if (newf)
                    out.appendLine("scenario, pChange, gamma, recovery_strategy, required_certainty, build_plans, matrices, visited_states, actions, time, timeout, cost")
                //val sNbAct = if (timeout) (nbActions+500).toDouble() else nbActions.toString()
                val sNbAct = nbActions.toString()
                out.appendLine("$scenario, ${"%.2f".format(pChange)}, ${"%.4f".format(gamma)}, $strategy, ${"%.2f".format(requiredCertainty)}, $nbPlanFor, $nbMatrices, $nbVisitedStates, $sNbAct, ${System.currentTimeMillis()-startT}, $toS, ${"%.2f".format(actionsCost)}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
