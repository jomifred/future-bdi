package jason.future

import jason.agent.NormativeAg
import jason.asSemantics.NoOptionException
import jason.asSemantics.Option
import jason.asSyntax.*
import jason.infra.local.RunLocalMAS
import jason.mas2j.AgentParameters
import jason.runtime.Settings
import java.io.*
import java.util.*

/**
 *  agent that considers the future to select (or not) option (of plans)
 *
 *  consider problems like loops in the behaviour, goal not achieved, norm violated, ...
 */
@Suppress("UNCHECKED_CAST")
open class ForeseeProblemAgent : NormativeAg(), StopConditions {

    /** required certainty to progress running matrix */
    private var rCertainty = 0.95

    override fun initAg() {
        super.initAg()
        try {
            val conf = Properties()
            try {
                val agC = (ts.settings.userParameters[Settings.PROJECT_PARAMETER] as AgentParameters).agClass
                var paramFleName = ""

                for (arg in agC.parameters) {
                    if (arg.endsWith(".npl\"")) continue // ignore npl file
                    println("Init Arg = $arg")
                    val l = ASSyntax.parseLiteral(arg)
                    paramFleName = (l.getTerm(0) as StringTerm).string
                    logger.info("*** loading parameters from $paramFleName")
                }
                if (paramFleName.isNotEmpty()) {
                    conf.load(FileReader(paramFleName))
                    rCertainty = conf.getOrDefault("requiredCertainty", rCertainty).toString().toDouble()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            logger.info("*** requiredCertainty = $rCertainty")
            StatData.requiredCertainty = rCertainty
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun userEnv() : MatrixCapable<*,*> = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment as MatrixCapable<*,*>

    fun envModel() : EnvironmentModel<State, Action>? {
        return try {
            userEnv().getModel() as EnvironmentModel<State, Action>
        } catch (e: NullPointerException) {
            null
        }
    }

    @Throws(NoOptionException::class)
    override fun selectOption(options: MutableList<Option>): Option? {
        if (envModel()?.hasGUI() == true) Search.solution.clear() // for GUI
        Search.clearVisited() // for GUI

        val defaultOption = super.selectOption(options) ?: return null
        //println("In select option for ${defaultOption.evt?.trigger}. Strategy = ${data.strategy}")

        if (ts.c.selectedEvent.intention == null // we are considering options only for an intention
            || defaultOption.evt.trigger.isFailureGoal) // do not use matrix for failure goals
            return defaultOption

        // simulates the future of options
        val search = Search(this, this, ExplorationStrategy.ONE, envModel()!!)
        search.init( listOf(defaultOption) )
        search.run()

        val failure = search.matrix.failure()
        if (failure != null) {
            val msg = "failure foreseen for handling ${defaultOption.evt.trigger} in the future! (states ahead: ${search.matrix.historyS})"
            logger.info("$msg -- $failure")
            throw NoOptionException(msg, ASSyntax.createLiteral("future_issue", failure))
        }

        if (envModel()?.hasGUI() == true) Search.solution.addAll(search.matrix.fo.states().first) // for GUI
        return defaultOption
    }

    // stop condition for matrix running
    override fun stop(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop : Boolean, certainty: Double) =
        steps > 5000 || certainty < rCertainty

    override fun failure(history: List<State>, steps: Int, stepsWithoutAct: Int, hasLoop: Boolean, agents: List<MatrixAgArch>): Literal? {
        for (agArch in agents) {
            if (agArch.ts.ag is MatrixAgent) {
                val unfuls = agArch.getAg().myUnfulfilledNorms()
                if (unfuls.isNotEmpty()) {
                    val norms = ListTermImpl()
                    for (ni in unfuls) {
                        norms.add(
                            ASSyntax.createLiteral("norm",
                                Atom(ni.norm.id),
                                ni.unifierAsTerm
                            ))
                    }
                    return ASSyntax.createLiteral("norm_unfulfilled", norms)
                }
            }
        }
        return super.failure(history, steps, stepsWithoutAct, hasLoop, agents)
    }
}
