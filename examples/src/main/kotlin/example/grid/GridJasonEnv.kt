package example.grid

// Environment code for project grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.NumberTerm
import jason.asSyntax.Structure
import jason.environment.Environment
import jason.environment.grid.GridWorldView
import jason.future.EnvironmentModel
import jason.future.MatrixCapable
import jason.runtime.RuntimeServicesFactory
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class GridJasonEnv : Environment(), MatrixCapable<GridState> {
    val model   = GridEnvModel(
        GridState(15, 7), // initial state
        GridState(15,17)  // goal  state
    )
    val view    = GridWorldView(model,  "Future!", 800)

    val log   = Logger.getLogger("grid-env")

    override fun init(args: Array<String>?) {
        view.isVisible = true

        if (args != null && args.size > 0) {
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
                    model.goalState = GridState(x,y)
                }
            }
        }
        thread(start = true) {
                // wait for some agent to be created
                while (RuntimeServicesFactory.get().agentsNames.isEmpty())
                    Thread.sleep(200)

                updateAgPercept( RuntimeServicesFactory.get().agentsNames.first() )
        }
    }

    override fun getModel(): EnvironmentModel<GridState> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        val prePos = model.getAgPos(0)
        model.execute( model.structureToAction(action) )
        log.info("executing: $action. from $prePos to ${model.getAgPos(0)}")
        Thread.sleep(300)
        updateAgPercept(agName)
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    private fun updateAgPercept(agName: String, ag: Int = 0) {
        clearPercepts(agName)
        addPercept(agName, *(model.agPerception(agName).toTypedArray()))
    }
}
