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

    private val BSF = false // TODO: use enum Exploration DFS, ONE, FIRST_LEVEL, NONE
    private val orderOptions = true // whether options are ordered before explored

    var originalAgent : ForeseeProblemAgent? = null
    var originalOption : Option? = null

    val explorationQueue = LinkedBlockingDeque<FutureOption>()

    //val visitedStates = mutableSetOf<State>()
    val visitedOption = mutableSetOf< Pair<State,String> >()


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

    /** returns true of the option should be explored */
    fun explore(o: Option) : Boolean {
        if (visitedOption.contains( Pair( envModel().currentState(), o.plan.label.functor)))
            return false
        return true
    }

    fun addToExplore(fo: FutureOption) {
//        println("+${fo.arch.env.currentState()}/${fo.o.plan.label.functor} in    $visitedOption")
        if (visitedOption.add( Pair(fo.arch.env.currentState(), fo.o.plan.label.functor))) {
            //visitedStates.add(fo.arch.env.currentState())
            if (BSF)
                explorationQueue.offerLast(fo) // for BSF
            else
                explorationQueue.offerFirst(fo) // for DSF
        }
    }

    override fun selectOption(options: MutableList<Option>): Option? {
        val defaultOption = super.selectOption(options)
        if (defaultOption == null)
            return null

        if (ts.c.selectedEvent.intention == null) // we are considering options only for an intention
            return defaultOption

//        println("options for ${ts.c.selectedEvent.trigger} in ${envModel().currentState()}" )

        if (inMatrix) {
            // store all options for further exploration (clone the agent and environment for each)
            if (firstSO && originalAgent?.curInt() == curInt()) { // consider only option for the original intention
                firstSO = false
                for (o in optionsCfParameter(options)) {
                    if (o != defaultOption && explore(o))
                        originalAgent?.addToExplore( prepareSimulation( o ))
                }
            }

            // do not consider the future in matrix mode
            return defaultOption
        }

        //visitedStates.clear()
        visitedOption.clear()
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

            println("\nstarting simulation for goal ${fo.evt.trigger.literal}@${fo.arch.env.currentState()} with plan @${fo.o.plan.label.functor}, I have ${explorationQueue.size} options still. Depth=${fo.ag.depth}")

            // run agent

            // add event / current option in the clone, so it continues from here
            fo.evt.option = fo.o
            fo.ag.ts.c.addEvent(fo.evt)
            fo.arch.run(fo.evt)

            if (!fo.arch.hasProblem()) {
                println("found an option with a likely nice future! $nbE options tried. plan=@${fo.ag.originalOption?.plan?.label?.functor}")
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
            envModel().clone(),
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