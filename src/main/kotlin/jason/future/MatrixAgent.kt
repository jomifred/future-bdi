package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Intention
import jason.asSemantics.Option

/** (main) agent running in the "matrix" */
class MatrixAgent(
    private val originalAgent : ForeseeProblemAgent,
    val originalOption : Option,
    val search: Search
) : PreferenceAgent() {

    init {
        setConsiderToAddMIForThisAgent(false)
    }

    private var firstSO  = true // if it is the first time this agent calls selectOption (in that cases, add FO)
    internal var myFO     : FutureOption? = null // the FO being tried by this agent

    var inZone1 = false

    internal fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch

    private fun envModel() : EnvironmentModel<State, Action> =
        myMatrixArch().env

    private fun curInt() : Intention = ts.c.selectedEvent.intention

    var lastFO : FutureOption? = null // used to build chain of selected (future) options

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options) ?: return null

        // store all options for further exploration (clone the agent and environment for each)
        if ((firstSO || inZone1)
            && originalAgent.curInt() == curInt()) { // consider only option for the original intention
            firstSO = false
            if (ForeseeProblemAgent.strategy() == ExplorationStrategy.SOLVE_P
                || ForeseeProblemAgent.strategy() == ExplorationStrategy.SOLVE_F
                || ForeseeProblemAgent.strategy() == ExplorationStrategy.SOLVE_M)
                for (o in options) {
                    if (o != defaultOption && search.shouldExplore(envModel().currentState(),o) ) {
                        search.expand(prepareSimulation(o, search))
                    }
                }
        }

        lastFO = prepareSimulation(defaultOption, search)
        // do not consider the future in matrix mode
        return defaultOption
    }

    fun costWeight() =
        if (inZone1)
            when (ForeseeProblemAgent.strategy()) {
                ExplorationStrategy.SOLVE_M -> 0.7
                ExplorationStrategy.SOLVE_F -> 0.0
                else -> 1.0
            }
        else 1.0


    private fun prepareSimulation(opt: Option, search: Search) : FutureOption {
        return FutureOption.build(opt, envModel(), originalAgent, originalOption, this,
            //myFO?.cost?:0.0, // no cost for any FO in original default option
            lastFO?.cost?:0.0,
            lastFO, costWeight(), search,
            myFO!!.otherAgs)
    }
}
