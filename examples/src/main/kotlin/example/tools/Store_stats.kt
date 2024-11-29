package example.tools

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.NumberTerm
import jason.asSyntax.Term
import jason.future.ForeseeProblemAgent
import kotlin.concurrent.thread

class store_stats : DefaultInternalAction() {

    override fun getMaxArgs(): Int = 0
    override fun getMinArgs(): Int = 0

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        //val solve   = args[0].toString().equals("solve")
        val timeout = (args[0] as NumberTerm).solve().toLong()
        if (timeout > 0) {
            thread(start = true) {
                Thread.sleep(timeout)
                ForeseeProblemAgent.expData.storeStats(true)
                System.exit(0)
            }
        } else {
            ForeseeProblemAgent.expData.storeStats(false)
            System.exit(0)
        }
        return true
    }
}
