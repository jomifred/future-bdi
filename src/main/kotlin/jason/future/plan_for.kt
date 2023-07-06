package jason.future

import jason.asSemantics.*
import jason.asSyntax.*
import java.lang.StringBuilder

class plan_for : DefaultInternalAction(), StopConditions {

    override fun getMaxArgs(): Int = 4
    override fun getMinArgs(): Int = 4

    //var baseIntention : Intention? = null

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        //println("** start building plan for ${args[0]}")
        try {
            // prepare arguments
            val goal = args[0] as Literal
            val ag = ts.ag as ForeseeProblemAgent
            val strategy = ExplorationStrategy.valueOf((args[3] as StringTerm).string)

            val initialPlan = (args[1] as Plan).capply(un)
            initialPlan.setAsPlanTerm(false)
            var initialPlanStr = initialPlan.toString()
            initialPlanStr = initialPlanStr.substring(0, initialPlanStr.length - 1) // remove last "."
            if (initialPlan.body.planSize == 0)
                initialPlanStr += " <- "
            else
                initialPlanStr += "; "

            // stats
            ForeseeProblemAgent.data.nbPlanFor++

            // run search using matrix
            val search = Search(ag, this, strategy, ag.envModel()!!)

            val te = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, goal)
            val relPlans = ts.relevantPlans(te, Event(te, buildBaseIntention(goal)))
            val appPlans = ts.applicablePlans(relPlans)

            search.init(appPlans)
            val opt = search.run()
            if (opt != null && search.matrix.success()) {
                val actionsStr = StringBuilder()
                for (a in opt.allActions()) {
                    actionsStr.append(a)
                    actionsStr.append("; ")
                }
                // build the new plan
                val newPlan = ASSyntax.parsePlan("$initialPlanStr $actionsStr .")
                newPlan.setAsPlanTerm(true)
                return un.unifies(args[2], newPlan)
            }
            ts.ag.logger.info("No plan found.")
            return un.unifies(args[2], Atom("no_plan"))
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun buildBaseIntention(goal: Literal) : Intention {
        val intention = Intention()
        val te1 = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, Atom("xxx"))
        val evt = Event(te1,intention)
        intention.push(IntendedMeans(
            Option(ASSyntax.parsePlan("+!xxx <- !${goal}."), Unifier(), evt),
            te1)
        )
        return intention
    }
}
