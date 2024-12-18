package jason.future

import jason.asSemantics.Intention
import jason.asSyntax.ASSyntax
import jason.asSyntax.Atom
import jason.asSyntax.Literal
import jason.asSyntax.NumberTermImpl
import jason.asSyntax.Structure

/** general interfaces for the Matrix Model */

/** simulates the future for an Intention (used just for testing) */
class Simulator<S : State> (
    private val env: EnvironmentModel<S, Action>,
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

interface EnvironmentModel<S : State, A : Action> : Cloneable {
    fun id() : String

    fun actions(): Collection<A>
    fun next(s:S, a:A): S
    fun currentState(): State
    fun agPerception(agName: String) : MutableCollection<Literal>
    fun execute(a:A): State

    /** the probability of going from s1 to s2 doing a */
    //fun gamma(s1:S, a:A, s2: S) : Double = 1.0

    /** certainty of next state */
    fun gamma() : Double = 1.0

    /** translates jason action as structure to Action of the model */
    fun structureToAction(agName: String, jasonAction: Structure) : Action

    fun hasGUI() : Boolean

    public override fun clone(): EnvironmentModel<S, A>
}

interface MatrixCapable <T: State, A: Action> {
    fun getModel() : EnvironmentModel<T, A>
}

interface State {
    /** get the current state as a list of perception */
    //fun asPerception(): MutableCollection<Literal>
}

open class Action(val name: String, val cost: Double) {
    override fun toString(): String = name
}

interface AgentModel<T : State> {
    fun decide(e: EnvironmentModel<T, Action>, s:T, goal:T): Action
}

/** stop conditions for the matrix */
private val hasLoopAtom = Atom("has_loop")

interface StopConditions {

    fun success(history: List<State>, steps: Int, intention: Intention) : Boolean =
        intention.isFinished
    fun failure(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop : Boolean, agents: List<MatrixAgArch>) : Literal? =
        if (hasLoop)
            hasLoopAtom
        else if (stepsWithoutAct > 200)
            ASSyntax.createLiteral("no_act_for", NumberTermImpl(stepsWithoutAct.toDouble()))
        else
            null

    fun stop(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop : Boolean, certainty: Double) : Boolean =
        steps > 5000

}