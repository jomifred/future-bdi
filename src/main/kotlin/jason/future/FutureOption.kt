package jason.future

import jason.asSemantics.Event
import jason.asSemantics.Option

/** the state for the search */
data class FutureOption(
    val evt: Event,     // event for which this FO was created
    val opt: Option,      // option where this FO was created
    val state: State,   // state where this FO was created
    val ag: MatrixAgent, // agent that will handle/simulate this FO
    val arch: MatrixAgentArch, // and  its arch
    val parent: FutureOption?, // FO that generated this one (to track back the root of exploration)
    val depth: Int = 0,
    val cost: Double, // accumulated cost until this FO
    val heuristic: Double = 0.0
) : Comparable<FutureOption> {

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
}