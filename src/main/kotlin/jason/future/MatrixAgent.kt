package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Agent
import jason.asSemantics.Event
import jason.asSemantics.Intention
import jason.asSemantics.Option

/** agent that run in the "matrix" */
class MatrixAgent(
    private val originalAgent : ForeseeProblemAgent,
    val originalOption : Option
) : PreferenceAgent() {

    private var firstSO  = true
    private var depth    = 1
    private var myFO     : FutureOption? = null // the FO being tried by this agent

    fun depth() = depth

    private fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch

    private fun envModel() : EnvironmentModel<State> =
        myMatrixArch().env

    private fun curInt() : Intention = ts.c.selectedEvent.intention

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options) ?: return null

        // store all options for further exploration (clone the agent and environment for each)
        if (firstSO && originalAgent.curInt() == curInt()) { // consider only option for the original intention
            firstSO = false
            if (originalAgent.strategy() == ExplorationStrategy.BFS || originalAgent.strategy() == ExplorationStrategy.DFS)
                for (o in originalAgent.optionsCfParameter(options)) {
                    if (o != defaultOption && originalAgent.explore(envModel().currentState(),o) ) {
                        originalAgent.addToExplore(prepareSimulation(o))
                    }
                }
        }

        // do not consider the future in matrix mode
        return defaultOption
    }

    private fun prepareSimulation(opt: Option) : FutureOption {
        // clone agent model (based on this agent)
        /*val agArch = MatrixAgentArch(
            envModel().clone(),
            "${ts.agArch.agName}_matrix"
        )
        val agModel = MatrixAgent(originalAgent, originalOption)
        this.cloneInto(agArch, agModel)
        agModel.ts.setLogger(agArch)
        agModel.depth = this.depth+1

        agModel.myFO = FutureOption(
            opt,
            agModel,
            agModel.myMatrixArch(),
            this.ts.c.selectedEvent.clone() as Event,
            this.myFO,
            this.envModel().currentState()
        )
        return agModel.myFO!!*/
        return buildAg(opt, envModel(), originalAgent, originalOption, this)
    }

    companion object {
        fun buildAg(opt : Option,
                    env: EnvironmentModel<State>,
                    originalAgent: ForeseeProblemAgent,
                    originalOption: Option,
                    parent: Agent
                    ) : FutureOption {
            val agArch = MatrixAgentArch(
                env.clone(),
                "${parent.ts.agArch.agName}_matrix"
            )
            val agModel = MatrixAgent(originalAgent, originalOption)
            parent.cloneInto(agArch, agModel)
            agModel.ts.setLogger(agArch)

            var thisFO: FutureOption? = null
            if (parent is MatrixAgent) {
                agModel.depth = parent.depth + 1
                thisFO = parent.myFO
            }

            agModel.myFO = FutureOption(
                parent.ts.c.selectedEvent.clone() as Event,
                opt,
                env.currentState(),
                agModel,
                agModel.myMatrixArch(),
                thisFO,
            )
            return agModel.myFO!!
        }
    }
}
