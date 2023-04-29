package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Event
import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS
import java.util.concurrent.LinkedBlockingDeque

/** agent that considers the future */
class ForeseeProblemAgent : PreferenceAgent() {

    private var inMatrix = false
    private var firstSO  = true
    private var depth    = 0

    private val BSF = true
    private val orderOptions = true

    var originalAgent : ForeseeProblemAgent? = null
    var originalOption : Option? = null

    val explorationQueue = LinkedBlockingDeque<FutureOption>()

    val visited = mutableSetOf<State>()

    fun optionsCfParameter(options: MutableList<Option>) : List<Option> =
        if (orderOptions)
            super.sortedOptions(options,BSF)
        else
            options

    fun myMatrixArch() : MatrixAgentArch = ts.agArch as MatrixAgentArch
    fun userEnv() : MatrixCapable<*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*>

    fun envModel() : EnvironmentModel<State> =
        if (inMatrix)
            myMatrixArch().env
        else
            userEnv().getModel() as EnvironmentModel<State>

    fun addToExplore(fo: FutureOption, prune: Boolean = true) {
        if (BSF)
            explorationQueue.offerLast(fo) // for BSF
        else
            explorationQueue.offerFirst(fo) // for DSF
    }

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options)
        if (defaultOption == null)
            return null

        if (ts.c.selectedEvent.intention == null) // we are considering options only for an intention
            return defaultOption

        if (inMatrix) {
            // store all options for further exploration (clone the agent and environment for each)
            if (firstSO && originalAgent?.curInt() == curInt()) { // consider only option for the original intention
                firstSO = false
                val arch = ts.agArch as MatrixAgentArch
                for (o in optionsCfParameter(options)) {
                    if (o != defaultOption)
                        originalAgent?.addToExplore( prepareSimulation( o ))
                }
                // the default option state is visited
                //visited.add( arch.env.currentState() )
            }
            // do not consider the future in matrix mode
            return defaultOption
        }

        val userEnv= RunLocalMAS.getRunner().environmentInfraTier.userEnvironment
        if (userEnv !is MatrixCapable<*>) // the environment should be Matrixable
            return defaultOption

        visited.clear()
        visited.add( userEnv.getModel().currentState())
        explorationQueue.clear()

        // simulates the future of options

        // clone agent and environment mode and add them into exploration queue

        // add (sorted) option to be explored
        // TODO: only add other options if default option is not ok; or use kind of lazy creation of this exploration  points
        for (o in optionsCfParameter(options)) {
            addToExplore(prepareSimulation(o))
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

    fun prepareSimulation(opt: Option) : FutureOption {
        // clone agent model (based on this agent)
        val agArch = MatrixAgentArch(
            envModel().clone(), // as EnvironmentModel<State>,
            "${ts.agArch.agName}_matrix"
        )
        val agModel = this.clone(agArch) as ForeseeProblemAgent
        agModel.inMatrix = true
        agModel.ts.setLogger(agArch)
        agModel.originalAgent = this.originalAgent?:this
        agModel.depth = this.depth+1
        agModel.originalOption = this.originalOption?:opt

        return FutureOption(
            opt,
            agModel,
            agArch,
            this.ts.c.selectedEvent.clone() as Event)
    }

    fun curInt() = ts.c.selectedEvent.intention
}

data class FutureOption(
    val o: Option,
    val ag: ForeseeProblemAgent,
    val arch: MatrixAgentArch,
    val evt: Event
)