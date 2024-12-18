package jason.future

import jason.agent.PreferenceAgent
import jason.agent.getCost
import jason.agent.getPreference
import jason.architecture.AgArch
import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Structure
import jason.infra.local.LocalAgArch
import jason.util.RunnableSerializable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** the state class for the search: represent a possible option in the future (as a starting point for matrix execution) */
data class FutureOption(
    val opt: Option,    // option where this FO was created
    val state: State,   // env state where this FO was created
    val certainty : Double, // certainty of this state
    val ag: MatrixAgent, // agent that will handle/simulate this FO
    val parent: FutureOption?, // FO that generated this one (to track back the root of exploration)
    val depth: Int = 0,
    val cost: Double, // accumulated cost until this FO
    val heuristic: Double = 0.0
) : Comparable<FutureOption> {

    val otherAgs : MutableMap<String,MatrixAgArch> = mutableMapOf()

    val actions = mutableListOf<Structure>()

    fun otherAgs() = otherAgs

    fun planId() : String = opt.plan.label.functor

    fun goal() = opt.evt.trigger.literal
    fun intention() = opt.evt.intention

    fun agArch() = ag.myMatrixArch()

    fun getPairId() = Pair( state, planId() )

    fun eval() = cost + heuristic

    override fun compareTo(other: FutureOption): Int =
        eval().compareTo(other.eval())

    fun planSize() = depth + agArch().historyS.size-2 // depth is steps before matrix, historyS is steps in matrix (minus initial state)

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
        while (f.parent != null) {
            states.add(0, f.state)
            f = f.parent!!
        }

        val beforePolicy = states.size
        val h = agArch().historyS
        for (i in 1 until h.size) {
            states.add( h[i] )
        }
        return Pair(states, beforePolicy)
    }

    fun allActions() : List<Structure> {
        val result = mutableListOf<Structure>()
        var f = this.parent
        while (f != null) {
            result.addAll(0, f.actions)
            f = f.parent
        }
        result.addAll(agArch().historyA)
        return result
    }

    companion object {

        private var agCounter = 0

        /** build a future option, clone agent/env, ... */
        fun build(  opt : Option,
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
            // clone env & agents for matrix
            val envClone = env.clone()
            val agArch  = MatrixAgArch(envClone,"${originalAgent.ts.agArch.agName}_matrix${agCounter++}")
            val agModel = MatrixAgent(originalAgent, originalOption, search)
            parent.cloneInto(agArch, agModel)
            agModel.ts.setLogger(agArch)

            agModel.myFO = FutureOption(
                opt.clone() as Option,
                env.currentState(),
                //(parentFO?.certainty?:1.0) * min(parentFO?.actions?.size?.toDouble()?:1.0,1.0) * env.gamma(), // the actions for parent FO may not be executed yet
                (parentFO?.certainty?:1.0) * env.gamma(),
                agModel,
                //agModel.myMatrixArch(),
                parentFO,
                (parentFO?.depth?:0) + 1,
                parentCost + costWeight * opt.getCost(),
                opt.getPreference()
            )
            agArch.myFO = agModel.myFO

            // add other agents
            for (agName in otherAgs.keys) {
                if (agName != originalAgent.ts.agArch.agName) {
                    val oArch = otherAgs[agName]!!
                    //println("adding agent $agName also in the matrix ${oArch.javaClass.name} / ${oArch.ts.ag.javaClass.name}")
                    val newArch = MatrixAgArch(envClone, "${agName}_matrix${agCounter++}")
                    val newModel = PreferenceAgent() // TODO: use the class of the oArch agent
                    newModel.setConsiderToAddMIForThisAgent(false)

                    // clone agent
                    if (oArch is MatrixAgArch) {
                        oArch.ts.ag.cloneInto(newArch, newModel)
                    } else if (oArch is LocalAgArch) { // it is an agent with its own thread and we cannot clone while running its reasoning cycle
                        val done = AtomicBoolean(false)
                        val lock = ReentrantLock()
                        val condition = lock.newCondition()
                        val code = RunnableSerializable {
                            oArch.ts.ag.cloneInto(newArch, newModel)
                            done.set(true)
                            lock.withLock { condition.signalAll() }
                        }
                        oArch.ts.runAtBeginOfNextCycle( code )
                        lock.withLock {
                            while (!done.get()) {
                                condition.await(50, TimeUnit.MILLISECONDS)
                            }
                        }
                        //println("Cloned!! ${newArch.ts.c.nbRunningIntentions} +${newArch.ts.c.events} +${newArch.ts.c.selectedEvent} +${newArch.ts.c.pendingActions.size}")

                        // Work around: set all pending actions as Ok (!!!!) -- since the env will notify the original ag about the completion of the action and not the clone
                        for (a in newArch.ts.c.pendingActions.values) {
                            a.result = true
                            newArch.ts.c.addFeedbackAction(a)
                        }
                    } else {
                        println("****** not sure clone works for arch class ${oArch.javaClass.name}")
                        oArch.ts.ag.cloneInto(newArch, newModel)
                    }

                    agModel.myFO!!.otherAgs[newArch.agName] = newArch
                }
            }
            return agModel.myFO!!
        }
    }
}
