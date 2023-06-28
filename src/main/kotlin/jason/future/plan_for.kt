package jason.future

import jason.asSemantics.DefaultInternalAction
import jason.asSemantics.Event
import jason.asSemantics.TransitionSystem
import jason.asSemantics.Unifier
import jason.asSyntax.*

class plan_for : DefaultInternalAction() {

    override fun getMaxArgs(): Int = 4
    override fun getMinArgs(): Int = 4

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        // prepare arguments
        val goal     = args[0] as Literal
        val ag       = ts.ag as ForeseeProblemAgent
        val strategy = ExplorationStrategy.valueOf( (args[3] as StringTerm).string )

        val initialPlan = (args[1] as Plan).capply(un)
        initialPlan.setAsPlanTerm(false)
        var initialPlanStr =  initialPlan.toString();
        initialPlanStr = initialPlanStr.substring(0, initialPlanStr.length-1) // remove last "."
        if (initialPlan.body.planSize == 0)
            initialPlanStr += " <- "

        // run search using matrix
        val search = Search(ag, strategy, ag.envModel())
        val te = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, goal)
        val relPlans = ts.relevantPlans(te, Event(te, ts.c.selectedIntention))
        val appPlans = ts.applicablePlans(relPlans)

        search.init(appPlans.get(0), appPlans)
        search.run()

        // build the solution
        val newPlan = ASSyntax.parsePlan("${initialPlanStr} ${ag.planBodyFound}.")
        newPlan.setAsPlanTerm(true)

        return un.unifies(args[2],newPlan)
    }

}
