package example.grid

// Environment code for project grid

import jason.asSyntax.Structure
import jason.environment.Environment
import jason.environment.grid.GridWorldView
import jason.future.EnvironmentModel
import jason.future.MatrixCapable
import jason.runtime.RuntimeServicesFactory
import java.util.logging.Logger
import kotlin.concurrent.thread

class GridJasonEnv : Environment(), MatrixCapable<GridLocation> {
    val model   = GridEnvModel(GridLocation(15,5))
    val view    = GridWorldView(model,  "Future!", 800)

    val log   = Logger.getLogger("grid-env")

    init {
        view.isVisible = true

        thread(start = true) {
                // wait for some agent to be created
                while (RuntimeServicesFactory.get().agentsNames.isEmpty())
                    Thread.sleep(300)

                updateAgPercept( RuntimeServicesFactory.get().agentsNames.first() )
        }
    }

    override fun getModel(): EnvironmentModel<GridLocation> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        log.info("executing: $action")
        model.execute( model.structureToAction(action) )
        Thread.sleep(200)
        updateAgPercept(agName)
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    private fun updateAgPercept(agName: String, ag: Int = 0) {
        clearPercepts(agName)
        addPercept(agName, *(model.agPerception(agName).toTypedArray()))
    }
}
