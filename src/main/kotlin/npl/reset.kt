package npl

import jason.agent.NormativeAg
import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.Term

class reset : DefaultInternalAction() {

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        if (ts.ag is NormativeAg) {
            val ag = ts.ag as NormativeAg
            //ag.logger.info("** removing all facts from NPL **")
            ag.resetNPL()
        }
        return true
    }
}
