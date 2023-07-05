package jason.future

import jason.agent.PreferenceAgent
import jason.agent.getCost
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

    var firstSO  = true // if it is the first time this agent calls selectOption (in that cases, add FO)
    internal var myFO     : FutureOption? = null // the FO being tried by this agent

    var inZone1 = false

    internal fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch

    private fun envModel() : EnvironmentModel<State, Action> = myMatrixArch().env

    //var lastFO : FutureOption? = null // used to build chain of selected (future) options

    var acumCost = -1.0

    override fun selectOption(options: MutableList<Option>): Option? {
        if (acumCost < 0) // fist time
            acumCost = myFO?.cost?:0.0

        val defaultOption = super.selectOption(options) ?: return null

        //println("original ${originalOption.evt.intention.id} current ${defaultOption.evt.intention.id}")
        // store all options for further exploration (clone the agent and environment for each)
        if ((firstSO || inZone1)
            && originalOption.evt.intention == defaultOption.evt.intention) { // consider only option for the original intention
            firstSO = false
            if (search.strategy == ExplorationStrategy.SOLVE_P
                || search.strategy == ExplorationStrategy.SOLVE_F
                || search.strategy == ExplorationStrategy.SOLVE_M) {
                for (o in options) {
                    if (o != defaultOption && search.shouldExplore(envModel().currentState(), o)) {
                        search.expand(prepareSimulation(o))
                    }
                }
            }
        }

        //lastFO = prepareSimulation(defaultOption) // just prepare, do not add for exploration, just to have lastFO
        acumCost += (costWeight() * defaultOption.getCost())
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


    private fun prepareSimulation(opt: Option) : FutureOption {
        //println("cost = "+acumCost+" ${lastFO?.cost}")
        return FutureOption.build(opt, envModel(), originalAgent, originalOption, this,
            //lastFO?.cost?:0.0,
            acumCost,
            //lastFO,
            myFO,
            costWeight(), search,
            myFO!!.otherAgs)
    }
}
