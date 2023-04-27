package example.grid

// Environment code for project grid

import jason.asSyntax.Structure
import jason.environment.Environment
import jason.environment.grid.GridWorldView
import jason.environment.grid.Location
import java.util.logging.Logger
import jason.future.Action


class GridJasonEnv : Environment {

    val model = GridEnvModel()
    val view  = GridWorldView(model,  "Future!", 800)

    val log   = Logger.getLogger("grid-env")

    constructor() { //}: super( emptyArray<String>() ) {
        view.setVisible(true)
    }

    override fun executeAction(agName: String, action: Structure): Boolean {
        log.info("executing: "+action)
        val agPos = model.getAgPos(0)
        val nextPos = model.next( GridLocation(agPos), Action(action.functor))
        model.setAgPos(0, nextPos.jgl)
        informAgsEnvironmentChanged()
        return true; // the action was executed with success
    }

}
