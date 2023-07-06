package jason.future

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.Term

class in_matrix : DefaultInternalAction() {

    override fun getMaxArgs(): Int = 1
    override fun getMinArgs(): Int = 1

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        return ts.agArch is MatrixAgentArch
    }
}
