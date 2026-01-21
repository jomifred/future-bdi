package jason.future

import jason.asSemantics.*
import jason.asSyntax.*
import java.lang.StringBuilder
import kotlin.random.Random

/** internal action that finds alternative plans that avoids future problems */

class plan_for : DefaultInternalAction(), StopConditions {

    override fun getMaxArgs(): Int = 4
    override fun getMinArgs(): Int = 5

    //var baseIntention : Intention? = null

    override fun execute(ts: TransitionSystem, un: Unifier, args: Array<out Term>): Any {
        //println("** start building plan for ${args[0]}")
        try {
            // prepare arguments
            val goal = args[0] as Literal
            val ag = ts.ag as ForeseeProblemAgent
            val strategy = ExplorationStrategy.valueOf((args[3] as StringTerm).string)
            var searchConds : StopConditions = this
            if (args.size > 4 && args[4].toString().equals("stop_cond(ag)"))
                searchConds = ag

            val initialPlan = (args[1] as Plan).capply(un)
            initialPlan.setAsPlanTerm(false)
            var initialPlanStr = initialPlan.toString()
            initialPlanStr = initialPlanStr.substring(0, initialPlanStr.length - 1) // remove last "."
            if (initialPlan.body.planSize == 0)
                initialPlanStr += " <- "
            else
                initialPlanStr += "; "
            suspend = false

            // run search using matrix
            val te = Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, goal)
            val relPlans = ts.ag.relevantPlans(te, Event(te, buildBaseIntention(goal)))
            val appPlans = ts.ag.applicablePlans(relPlans)
            if (appPlans.isEmpty()) {
                ts.ag.logger.info("No applicable plan for ${goal}! So, no plan considering the future.")
                return false
            }

            // run search using matrix
            if (strategy != ExplorationStrategy.RANDOM) {
                val search = Search(ag, searchConds, strategy, ag.envModel()!!)
                search.init(appPlans)
                val opt = search.run()
                if (opt != null && search.matrix.success()) {
                    val actionsStr = StringBuilder()
                    for (a in opt.allActions()) {
                        actionsStr.append(a)
                        actionsStr.append("; ")
                    }

                    StatData.nbPlanFor++ // stats

                    // build the new plan
                    val newPlan = ASSyntax.parsePlan("$initialPlanStr $actionsStr .")
                    newPlan.setAsPlanTerm(true)
                    return un.unifies(args[2], newPlan)
                }
            }

            //val defOpt = ag.sortedOptions(appPlans).first() // may cause loop em behaviour
            //val defOpt = search.bestFO.ag.originalOption // takes the seen option
            var defOpt = appPlans.random() // always works (!)
            if (Random.nextDouble() < 0.7) // but prefer my policy anyway
                defOpt = ag.sortedOptions(appPlans).first()

            val intention = ts.c.selectedIntention
            intention.pop() // remove the IM running this internal action (!!)
            suspend = true // so that the TS will not change the intention
            intention.push(IntendedMeans(defOpt, defOpt.evt.trigger)) // add default option on top
            ts.c.addRunningIntention(intention)
            //ts.ag.logger.info("No plan but first option is ${defOpt.plan.label.functor} for $intention")

            ts.ag.logger.info("No plan found, using random option.")
            return true //un.unifies(args[2], Atom("no_plan"))
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    var suspend = false
    override fun suspendIntention(): Boolean = suspend

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
