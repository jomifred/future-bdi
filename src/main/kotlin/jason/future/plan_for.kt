package jason.future

import jason.asSemantics.*
import jason.asSyntax.*

class plan_for : DefaultInternalAction() {

    override fun getMaxArgs(): Int = 4
    override fun getMinArgs(): Int = 4

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        println("** start building plan for ${args[0]}")
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

            // run search using matrix
            val search = Search(ag, strategy, ag.envModel())

            val te = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, goal)
            val relPlans = ts.relevantPlans(te, Event(te, baseIntention(goal)))
            val appPlans = ts.applicablePlans(relPlans)

            search.init(appPlans.get(0), appPlans)
            search.run()

            // build the new plan
            val newPlan = ASSyntax.parsePlan("${initialPlanStr} ${ag.planBodyFound}.")
            newPlan.setAsPlanTerm(true)

            println("** created plan = ${newPlan}")

            return un.unifies(args[2], newPlan)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun baseIntention(goal: Literal) : Intention {
        val te1 = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, Atom("xxx"))
        val intention = Intention()
        val evt = Event(te1,intention)
        intention.push(IntendedMeans(
            Option(ASSyntax.parsePlan("+!xxx <- !${goal}."), Unifier(), evt),
            te1)
        )
        return intention
    }
}
