package example.grid

// Environment code for project grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.Structure
import jason.environment.Environment
import jason.environment.grid.GridWorldModel
import jason.environment.grid.GridWorldView
import jason.future.Action
import jason.runtime.RuntimeServicesFactory
import java.util.logging.Logger
import kotlin.concurrent.thread


class GridJasonEnv : Environment() {

    val model   = GridEnvModel()
    val actions = model.actions().associateBy { it.name }
    val view    = GridWorldView(model,  "Future!", 800)

    val log   = Logger.getLogger("grid-env")

    init {
        view.isVisible = true

        thread(start = true) {
        //GlobalScope.launch {
                // wait for some agent to be created
                while (RuntimeServicesFactory.get().agentsNames.isEmpty())
                    Thread.sleep(300)
                    //delay(300L)

                updateAgPercept( RuntimeServicesFactory.get().agentsNames.first() )

                // its destination
                addPercept(ASSyntax.createLiteral(
                    "destination",
                    ASSyntax.createNumber(15.0),
                    ASSyntax.createNumber(25.0),
                ))
                addPercept(ASSyntax.createLiteral(
                    "w_size",
                    ASSyntax.createNumber(model.width.toDouble()),
                    ASSyntax.createNumber(model.height.toDouble()),
                ))
        }
    }

    override fun executeAction(agName: String, action: Structure): Boolean {
        log.info("executing: $action")
        val agPos   = model.getAgPos(0)
        val nextPos = model.next( GridLocation(agPos), actions.get( action.functor) as Action)
        model.setAgPos(0, nextPos.l)
        Thread.sleep(200)
        updateAgPercept(agName)
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    private fun updateAgPercept(agName: String, ag: Int = 0) {
        clearPercepts(agName)

        // its location
        val l = model.getAgPos(ag)
        addPercept(agName, ASSyntax.createLiteral(
            "pos",
            ASSyntax.createNumber(l.x.toDouble()),
            ASSyntax.createNumber(l.y.toDouble()),
        ))

        // what's around
        updateAgPercept(agName, l.x - 1, l.y - 1)
        updateAgPercept(agName, l.x - 1, l.y)
        updateAgPercept(agName, l.x - 1, l.y + 1)
        updateAgPercept(agName, l.x, l.y - 1)
        updateAgPercept(agName, l.x, l.y)
        updateAgPercept(agName, l.x, l.y + 1)
        updateAgPercept(agName, l.x + 1, l.y - 1)
        updateAgPercept(agName, l.x + 1, l.y)
        updateAgPercept(agName, l.x + 1, l.y + 1)
    }

    private fun updateAgPercept(agName: String, x: Int, y: Int) {
        //if (random.nextDouble() < model.getAgFatigue(agId)) return  // perception omission
        //if (model == null || !model.inGrid(x, y)) return
        if (model.hasObject(GridWorldModel.OBSTACLE, x, y)) {
            addPercept(agName, ASSyntax.createLiteral(
                "obstacle",
                ASSyntax.createNumber(x.toDouble()),
                ASSyntax.createNumber(y.toDouble()),
            ))
        }
    }

}
