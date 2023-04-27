package jason.future

/** simulates the future for an Intention */
class Simulator<S : State> (
    val env: EnvironmentModel<S>,
    val ag : AgentModel<S>
) {

    fun simulate(init: S, goal: S): List<Pair<S,Action>> {
        fun sim(path: List<Pair<S, Action>>, s: S) =
            if (s == goal)
                path
            else {
                ag.decide(env, s, goal)
                    .let { path + Pair(s, it) + simulate(env.next(s, it), goal) }
            }
        return sim(listOf(), init)
    }
}

interface EnvironmentModel<T : State>  {
    fun actions(): List<Action>
    fun next(s:T, a:Action): T
}

interface State {}

class Action(val name: String) {
    override fun toString(): String = name
}

interface AgentModel<T : State> {
    fun decide(e: EnvironmentModel<T>, s:T, goal:T): Action
}