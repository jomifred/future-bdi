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
    private var originalOption : Option? = null

    var originalAgent : ForeseeProblemAgent? = null

    val explorationQueue = LinkedBlockingDeque<FutureOption>()

    val visited = mutableSetOf<State>()

    fun addToExplore(fo: FutureOption, prune: Boolean = true) {
        if (depth != 0) println("!!!!!!!!")
        //println("    add ${fo.state}, visited: $visited")
        //if (prune && visited.contains(fo.state)) return
        //visited.add(fo.state)
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

        if (inMatrix) {
            // store all options for further exploration (clone the agent and environment for each)
            if (firstSO && originalAgent?.curInt() == curInt()) { // consider only option for the original intention
                firstSO = false
                val arch = ts.agArch as MatrixAgentArch
                for (o in options) {
                    //println("in matrix options $depth")
                    originalAgent?.addToExplore(
                        prepareSimulation( arch.env.clone(), o, this)
                    )
                }
                // the default option state is visited
                //visited.add( arch.env.currentState() )
            }
            // do not consider the future in matrix mode
            return defaultOption
        }

        visited.clear()
        visited.add( userEnv.getModel().currentState())
        explorationQueue.clear()

        // simulates the future of options

        // clone agent and environment mode and add them into exploration queue

        // add default option as the first to be explored
        addToExplore( prepareSimulation(
            userEnv.getModel().clone() as EnvironmentModel<State>,
            defaultOption,
            this), false) // do not prune first level search

        // add other options // TODO: only add other options if default option is not ok; or  use kind of  lazy creation of this exploration  points
        for (o in options) {
            addToExplore( prepareSimulation(
                userEnv.getModel().clone() as EnvironmentModel<State>,
                o,
                this), false )
        }

        // explore options to see their future
        var nbE = 0
        var fo = explorationQueue.poll()
        while (fo != null && nbE < 10000) { // TODO: add a parameter somewhere to define o max number os options to explore
            nbE++

            println("starting simulation for ${fo.evt.trigger.literal} with ${fo.o.plan.label.functor}, I have ${explorationQueue.size} options still. Depth = ${fo.ag.depth}")

            // run agent

            // add event / current option in the clone, so it continues from here
            fo.evt.option = fo.o
            fo.ag.ts.c.addEvent(fo.evt)
            fo.arch.run(fo.evt)

            if (!fo.arch.hasProblem()) {
                println("found an option with a likely nice future! ${nbE} options tried.")
                return fo.ag.originalOption
            }
            fo = explorationQueue.poll()
        }
        println("\nsorry, all options have an unpleasant future. aborting the intention! (tried $nbE options)\n")
        return null
    }

    //fun prepareSimulation(userEnv: MatrixCapable<*>, opt: Option, ag: ForeseeProblemAgent) : FutureOption {
    fun prepareSimulation(envModel: EnvironmentModel<State>, opt: Option, ag: ForeseeProblemAgent) : FutureOption {
        //val envModel = userEnv.getModel().clone()
        //println("env $envModel ${RunLocalMAS.getRunner().environmentInfraTier.userEnvironment}")

        // clone agent model (based on this agent)
        val agArch = MatrixAgentArch(
            envModel, // as EnvironmentModel<State>,
            "${ts.agArch.agName}_matrix"
        )
        val agModel = ag.clone(agArch) as ForeseeProblemAgent
        agModel.inMatrix = true
        agModel.ts.setLogger(agArch)
        agModel.originalAgent = ag.originalAgent?:ag
        agModel.depth = ag.depth+1
        agModel.originalOption = ag.originalOption?:opt

        return FutureOption(
            opt,
            agModel,
            agArch,
            ag.ts.c.selectedEvent.clone() as Event,
            envModel.currentState())
    }

    fun curInt() = ts.c.selectedEvent.intention
}

data class FutureOption(
    val o: Option,
    val ag: ForeseeProblemAgent,
    val arch: MatrixAgentArch,
    val evt: Event,
    val state: State
)