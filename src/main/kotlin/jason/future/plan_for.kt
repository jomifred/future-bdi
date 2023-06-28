package jason.future

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.asSyntax.Term

class plan_for : DefaultInternalAction() {

    override fun getMaxArgs(): Int = 2
    override fun getMinArgs(): Int = 2

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        val goal = args[0] as Literal

//        val search = Search(this, ForeseeProblemAgent.solveStrategy, envModel())
//        search.init(defaultOption, options)
//        val opt = search.run()
//        if (opt == null) {
//            throw NoOptionException("there will be a failure to handle ${curInt().peek()?.trigger?:"this intention"} in the future! (states ahead: ${search.matrix.historyS})", ASSyntax.createAtom("no_future"))
//        }
//        return opt

        val newPlan = ASSyntax.parsePlan("+!${goal} <- a;b;c.")
        newPlan.setAsPlanTerm(true)

        return un.unifies(args[1],newPlan)
    }

}
