package jason.future

import jason.agent.PreferenceAgent
import jason.agent.getCost
import jason.agent.getPreference
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
    private var myFO     : FutureOption? = null // the FO being tried by this agent

    private fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch

    private fun envModel() : EnvironmentModel<State> =
        myMatrixArch().env

    private fun curInt() : Intention = ts.c.selectedEvent.intention

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options) ?: return null

        // store all options for further exploration (clone the agent and environment for each)
        if (firstSO && originalAgent.curInt() == curInt()) { // consider only option for the original intention
            firstSO = false
            if (ForeseeProblemAgent.strategy() == ExplorationStrategy.SOLVE_P || ForeseeProblemAgent.strategy() == ExplorationStrategy.SOLVE_F)
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
        return buildAg(opt, envModel(), originalAgent, originalOption, this)
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
                    parent: Agent
                    ) : FutureOption {
            val agArch = MatrixAgentArch(
                env.clone(),
                "${parent.ts.agArch.agName}_matrix${agCounter++}"
            )
            val agModel = MatrixAgent(originalAgent, originalOption)
            parent.cloneInto(agArch, agModel)
            agModel.ts.setLogger(agArch)

            var parentFO: FutureOption? = null
            var parentDepth = 0
            var parentCost  = 0.0
            if (parent is MatrixAgent) {
                parentFO    = parent.myFO
                parentDepth = parent.myFO?.depth?:0
                parentCost  = parent.myFO?.cost?:0.0
            }

            agModel.myFO = FutureOption(
                parent.ts.c.selectedEvent.clone() as Event,
                opt,
                env.currentState(),
                agModel,
                agModel.myMatrixArch(),
                parentFO,
                parentDepth + 1,
                parentCost + opt.getCost(),
                opt.getPreference()
            )
            return agModel.myFO!!
        }
    }
}

data class FutureOption(
    val evt: Event,     // event for which this FO was created
    val opt: Option,      // option where this FO was created
    val state: State,   // state where this FO was created
    val ag: MatrixAgent, // agent that will handle/simulate this FO
    val arch: MatrixAgentArch, // and  its arch
    val parent: FutureOption?, // FO that generated this one (to track back the root of exploration)
    val depth: Int = 0,
    val cost: Double,
    val heuristic: Double = 0.0
) : Comparable<FutureOption> {

    fun planId() : String = opt.plan.label.functor

    fun getPairId() = Pair( arch.env.currentState(), planId())

    fun eval() = cost + heuristic

    override fun compareTo(other: FutureOption): Int =
        eval().compareTo(other.eval())

    override fun hashCode(): Int {
        return state.hashCode() + (planId().hashCode()*31)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is FutureOption) return state == other.state && planId() == other.planId()
        return false
    }
}