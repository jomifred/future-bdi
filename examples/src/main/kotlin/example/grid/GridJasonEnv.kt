package example.grid

// Environment code for project grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.NumberTerm
import jason.asSyntax.StringTerm
import jason.asSyntax.Structure
import jason.environment.Environment
import jason.future.*
import jason.runtime.RuntimeServicesFactory
import java.io.FileReader
import java.util.*
import java.util.logging.Logger
import kotlin.concurrent.thread

open class GridJasonEnv : Environment(), MatrixCapable<GridState, Action> {
    protected var model   = GridEnvModel(
        GridState(15, 5), // initial state
        GridState(15,17)  // goal  state
    )
    protected val log :Logger  = Logger.getLogger("grid-env")
    protected var view: GridEnvView? = null

    override fun init(args: Array<String>?) {
        var gui = true
        if (!args.isNullOrEmpty()) {
            for (a in args) {
                if (a.startsWith("init")) {
                    val l = ASSyntax.parseLiteral(a)
                    val x = (l.getTerm(0) as NumberTerm).solve().toInt()
                    val y = (l.getTerm(1) as NumberTerm).solve().toInt()
                    model.setInitState( GridState(x,y) )
                }
                if (a.startsWith("goal")) {
                    val l = ASSyntax.parseLiteral(a)
                    val x = (l.getTerm(0) as NumberTerm).solve().toInt()
                    val y = (l.getTerm(1) as NumberTerm).solve().toInt()
                    model.setGoal( GridState(x,y) )
                }
                if (a.startsWith("scenario")) {
                    val l = ASSyntax.parseLiteral(a)
                    when ( (l.getTerm(0) as StringTerm).string) {
                        "--" -> model.setScenarioWalls(0)
                        "U"  -> model.setScenarioWalls(1)
                        "H"  -> model.setScenarioWalls(2)
                        "O"  -> model.setScenarioWalls(3)
                    }
                }
                if (a.startsWith("no_gui")) {
                    gui = false
                }

                try {
                    val conf = Properties()
                    conf.load(FileReader("params.properties"))
                    setStrategy(conf.getOrDefault("recover_strategy", "NONE").toString())
                } catch (e: Exception) {
                    setStrategy(ExplorationStrategy.SOLVE_M)
                    e.printStackTrace()
                }
            }
        }
        if (gui) {
            view = GridEnvView(model, this)
            view?.resetGUI()
        } else {
            delay = 0
        }
        thread(start = true) {
                // wait for some agent to be created
                while (RuntimeServicesFactory.get().agentsName.isEmpty())
                    Thread.sleep(200)

                updatePercept()
        }
    }

    override fun getModel(): EnvironmentModel<GridState, Action> = model

    fun getStrategy() = ForeseeProblemAgent.expData.strategy

    fun setStrategy(s: ExplorationStrategy) {
        ForeseeProblemAgent.expData.strategy = s
    }
    fun setStrategy(s: String) {
        setStrategy(ExplorationStrategy.valueOf(s))
    }

    var delay : Long = 100

    override fun executeAction(agName: String, action: Structure): Boolean {
        val prePos = model.getAgPos(0)
        val a = model.structureToAction(agName, action)

        model.execute( a )

        ForeseeProblemAgent.expData.nbActions++ // stats
        ForeseeProblemAgent.expData.actionsCost += a.cost

        log.info("executing: $action. from $prePos to ${model.getAgPos(0)}")
        if (delay>0)
            Thread.sleep(delay)
        updateAgPercept(agName)
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    fun updatePercept() {
        if (RuntimeServicesFactory.get().agentsName.isNotEmpty()) {
            updateAgPercept(RuntimeServicesFactory.get().agentsName.first())
            informAgsEnvironmentChanged()
        }
    }

    internal fun updateAgPercept(agName: String) {
        clearPercepts(agName)
        addPercept(agName, *(model.agPerception(agName).toTypedArray()))
        addPercept(agName, ASSyntax.parseLiteral("r_strategy(\"${ForeseeProblemAgent.expData.strategy}\")"))
    }
}
