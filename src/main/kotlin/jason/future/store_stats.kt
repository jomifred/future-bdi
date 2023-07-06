package jason.future

import jason.asSemantics.*
import jason.asSyntax.*
import java.lang.StringBuilder

class store_stats : DefaultInternalAction(), StopConditions {

    override fun getMaxArgs(): Int = 0
    override fun getMinArgs(): Int = 0

    //var baseIntention : Intention? = null

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        ForeseeProblemAgent.data.storeStats()
        return true
    }
}
