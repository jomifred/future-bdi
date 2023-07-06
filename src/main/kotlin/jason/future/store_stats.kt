package jason.future

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.Term

class store_stats : DefaultInternalAction(), StopConditions {

    override fun getMaxArgs(): Int = 0
    override fun getMinArgs(): Int = 0

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        ForeseeProblemAgent.data.storeStats()
        return true
    }
}
