package jason.future

import jason.agent.PreferenceAgent
import jason.agent.getCost
import jason.agent.getPreference
import jason.asSemantics.Agent
import jason.asSemantics.Event
import jason.asSemantics.Intention
import jason.asSemantics.Option

/** (main) agent running in the "matrix" */
class MatrixAgent(
    private val originalAgent : ForeseeProblemAgent,
    val originalOption : Option,
    val search: Search
) : PreferenceAgent() {

    private var firstSO  = true // if it is the first time this agent calls selectOption (in that cases, add FO)
    private var myFO     : FutureOption? = null // the FO being tried by this agent

    var inZone1 = false

    private fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch

    private fun envModel() : EnvironmentModel<State> =
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
        return buildAg(opt, envModel(), originalAgent, originalOption, this,
            //myFO?.cost?:0.0, // no cost for any FO in original default option
            lastFO?.cost?:0.0,
            lastFO, costWeight(), search)
    }

    override fun addToMindInspectorWeb() {
        // do not add
    }

    companion object {

        private var agCounter = 0

        /** build a future option, clone agent/env, ... */
        fun buildAg(opt : Option,
                    env: EnvironmentModel<State>,
                    originalAgent: ForeseeProblemAgent,
                    originalOption: Option,
                    parent: Agent,
                    parentCost : Double,
                    parentFO : FutureOption?,
                    costWeight : Double,
                    search: Search
                    ) : FutureOption {
            val agArch = MatrixAgentArch(
                env.clone(),
                "${parent.ts.agArch.agName}_matrix${agCounter++}"
            )
            val agModel = MatrixAgent(originalAgent, originalOption, search)
            parent.cloneInto(agArch, agModel)
            agModel.ts.setLogger(agArch)

            agModel.myFO = FutureOption(
                parent.ts.c.selectedEvent.clone() as Event,
                opt,
                env.currentState(),
                agModel,
                agModel.myMatrixArch(),
                parentFO,
                (parentFO?.depth?:0) + 1,
                parentCost + costWeight * opt.getCost(),
                opt.getPreference()
            )
            return agModel.myFO!!
        }
    }
}
