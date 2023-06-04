package jason.future

import jason.asSyntax.Literal
import jason.asSyntax.Structure

/** simulates the future for an Intention */
class Simulator<S : State> (
    private val env: EnvironmentModel<S>,
    private val ag : AgentModel<S>
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

interface EnvironmentModel<T : State> : Cloneable {
    fun id() : String

    fun actions(): Collection<Action>
    fun next(s:T, a:Action): T
    fun currentState(): State
    fun agPerception(agName: String) : MutableCollection<Literal>
    fun execute(a:Action): State
    /** translates jason action as structure to Action of the model */
    fun structureToAction(jasonAction: Structure) : Action

    public override fun clone(): EnvironmentModel<T>
}

interface MatrixCapable <T: State> {
    fun getModel() : EnvironmentModel<T>
}

interface State {
    /** get the current state as a list of perception */
    //fun asPerception(): MutableCollection<Literal>
}

class Action(val name: String) {
    override fun toString(): String = name
}

interface AgentModel<T : State> {
    fun decide(e: EnvironmentModel<T>, s:T, goal:T): Action
}

