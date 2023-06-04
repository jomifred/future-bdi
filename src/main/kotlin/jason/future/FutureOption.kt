package jason.future

import jason.agent.PreferenceAgent
import jason.agent.getCost
import jason.agent.getPreference
import jason.architecture.AgArch
import jason.asSemantics.Agent
import jason.asSemantics.Event
import jason.asSemantics.Option
import jason.infra.local.LocalAgArch
import java.util.concurrent.atomic.AtomicBoolean

/** the state for the search */
data class FutureOption(
    val evt: Event,     // event for which this FO was created
    val opt: Option,    // option where this FO was created
    val state: State,   // env state where this FO was created
    val ag: MatrixAgent, // agent that will handle/simulate this FO
    val arch: MatrixAgentArch, // and its arch
    val parent: FutureOption?, // FO that generated this one (to track back the root of exploration)
    val depth: Int = 0,
    val cost: Double, // accumulated cost until this FO
    val heuristic: Double = 0.0
) : Comparable<FutureOption> {

    val otherAgs : MutableMap<String,MatrixAgentArch> = mutableMapOf()

    fun otherAgs() = otherAgs

    fun planId() : String = opt.plan.label.functor

    fun getPairId() = Pair( arch.env.currentState(), planId())

    fun eval() = cost + heuristic

    override fun compareTo(other: FutureOption): Int =
        eval().compareTo(other.eval())

    fun planSize() = depth + arch.historyS.size-2 // depth is steps before matrix, historyS is steps in matrix (minus initial state)

    override fun hashCode(): Int {
        return state.hashCode() + (planId().hashCode()*31)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is FutureOption) return state == other.state && planId() == other.planId()
        return false
    }

    fun states() : Pair<List<State>, Int> {
        val states = mutableListOf<State>()
        var f = this
        //states.add(0, f.state)
        while (f.parent != null) {
            states.add(0, f.state)
            f = f.parent!!
        }

        val beforePolicy = states.size
        //states.add(M())
        val h = arch.historyS
        for (i in 1 until h.size) {
            states.add( h[i] )
        }
        return Pair(states, beforePolicy)
    }

    /*class M : State {
        override fun toString(): String {
            return "---"
        }
    }*/

    companion object {

        private var agCounter = 0

        /** build a future option, clone agent/env, ... */
        fun build(opt : Option,
                    env: EnvironmentModel<State, Action>,
                    originalAgent: ForeseeProblemAgent,
                    originalOption: Option,
                    parent: Agent,
                    parentCost : Double,
                    parentFO : FutureOption?,
                    costWeight : Double,
                    search: Search,
                    otherAgs: Map<String, AgArch>
        ) : FutureOption {
            val envClone = env.clone()
            val agArch = MatrixAgentArch(envClone,"${parent.ts.agArch.agName}_matrix${agCounter++}")
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

            // add other agents
            for (agName in otherAgs.keys) {
                if (!agName.equals( originalAgent.ts.agArch.agName)) {
                    val oArch = otherAgs.get(agName)!!
                    //println("adding agent $agName also in the matrix ${oArch.javaClass.name}")
                    val newArch = MatrixAgentArch(envClone, "${agName}_matrix${agCounter++}")
                    val newModel = PreferenceAgent() // TODO: use the class of the oArch agent
                    newModel.setConsiderToAddMIForThisAgent(false)

                    // clone agent
                    if (oArch is MatrixAgentArch) {
                        oArch.ts.ag.cloneInto(newArch, newModel)
                    } else { // it is an agent with its own thread and we cannot clone while running its reasoning cycle
                        val done = AtomicBoolean(false)
                        oArch.ts.runAtBeginOfNextCycle({
                            oArch.ts.ag.cloneInto(newArch, newModel)
                            done.set(true)
                        })
                        while (!done.get()) {
                            println("in wait")
                            Thread.sleep(100) // TODO: use wait notify
                        }
                    }

                    agModel.myFO!!.otherAgs[newArch.agName] = newArch
                }
            }
            return agModel.myFO!!
        }
    }
}
