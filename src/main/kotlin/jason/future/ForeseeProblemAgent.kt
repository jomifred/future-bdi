package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Event
import jason.asSemantics.Option
import jason.environment.Environment
import jason.infra.local.RunLocalMAS
import java.util.concurrent.LinkedBlockingDeque

/** agent that considers the future */
class ForeseeProblemAgent : PreferenceAgent() {

    private var inMatrix = false
    private var firstSO  = true
    private var depth    = 0

    var originalAgent : ForeseeProblemAgent? = null

    val explorationQueue = LinkedBlockingDeque<FutureOption>()

    fun addToExplore(fo: FutureOption) {
        if (depth != 0) println("!!!!!!!!")
        explorationQueue.offerLast(fo)
    }

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options)
        if (defaultOption == null)
            return null

        val userEnv= RunLocalMAS.getRunner().environmentInfraTier.userEnvironment
        if (userEnv !is MatrixCapable<*>) // the environment should be Matrixable
            return defaultOption
        if (ts.c.selectedEvent.intention == null) // we are considering options only for an intention
            return defaultOption

        options.remove(defaultOption)

//        if (depth >= 2)
//            println("***************"+inMatrix+" $firstSO ${options.size} $originalAgent")
        if (inMatrix) {
            // store all options for further exploration (clone the agent and environment for each)
            if (firstSO) {
                firstSO = false
                for (o in options) {
                    //println("in matrix options ${originalAgent}")
                    originalAgent?.addToExplore(
                        prepareSimulation( userEnv, o, this)
                    )
                }
            }
            // do not consider the future in matrix mode
            return defaultOption
        }

        // simulates the future of default options

        // clone agent and environment mode and add them into exploration queue

        // add default option as the first to be explored
        explorationQueue.offerLast( prepareSimulation(userEnv, defaultOption, this))

        // add other options // TODO: only add other options if default option is not ok; or  use kind of  lazy creation of this exploration  points
        for (o in options) {
            explorationQueue.offerLast( prepareSimulation(userEnv, o, this) )
        }

        // explore options to see their future
        var nbE = 0
        var fo = explorationQueue.poll()
        while (fo != null && nbE < 5000) { // TODO: add a parameter somewhere to define o max number os options to explore
            nbE++

            println("starting simulation for ${fo.evt.trigger.literal} with ${fo.o.plan.label.functor}, I have ${explorationQueue.size} options still. Depth = ${fo.ag.depth}")

            // run agent

            // add event / current option in the clone, so it continues from here
            fo.evt.option = fo.o
            fo.ag.ts.c.addEvent(fo.evt)
            fo.arch.run(fo.evt)

            if (!fo.arch.hasProblem()) {
                println("found an option with a likely nice future! ${nbE} options tried.")
                explorationQueue.clear()
                // TODO: should not return fo.o (that is an option in the future, but the current option that produced that future option
                return fo.o
            }
            fo = explorationQueue.poll()
        }
        println("\nsorry, all options have an unpleasant future. aborting the intention! (tried $nbE options)\n")
        return null
    }

    fun prepareSimulation(userEnv: MatrixCapable<*>, opt: Option, ag: ForeseeProblemAgent) : FutureOption {
        val envModel = userEnv.getModel().clone()
        //println("env $envModel ${RunLocalMAS.getRunner().environmentInfraTier.userEnvironment}")

        // clone agent model (based on this agent)
        val agArch = MatrixAgentArch(
            envModel as EnvironmentModel<State>,
            "${ts.agArch.agName}_matrix"
        )
        val agModel = ag.clone(agArch) as ForeseeProblemAgent
        agModel.inMatrix = true
        agModel.ts.setLogger(agArch)
        agModel.originalAgent = ag.originalAgent?:ag
        agModel.depth = ag.depth+1

        return FutureOption(opt, agModel, agArch, ag.ts.c.selectedEvent.clone() as Event)
    }

}

data class FutureOption(val o: Option, val ag: ForeseeProblemAgent, val arch: MatrixAgentArch, val evt: Event)