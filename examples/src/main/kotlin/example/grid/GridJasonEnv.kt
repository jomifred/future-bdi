package example.grid

// Environment code for project grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.Structure
import jason.environment.Environment
import jason.environment.grid.GridWorldModel
import jason.environment.grid.GridWorldView
import jason.future.Action
import java.util.logging.Logger


class GridJasonEnv() : Environment() {

    val model   = GridEnvModel()
    val actions = model.actions().associate { it.name to it }
    val view    = GridWorldView(model,  "Future!", 800)

    val log   = Logger.getLogger("grid-env")

    init {
        view.setVisible(true)
        updateAgPercept("robot1",0)
    }

    override fun executeAction(agName: String, action: Structure): Boolean {
        log.info("executing: "+action)
        val agPos   = model.getAgPos(0)
        val nextPos = model.next( GridLocation(agPos), actions.get( action.functor) as Action)
        model.setAgPos(0, nextPos.jgl)
        Thread.sleep(200)
        updateAgPercept("robot1",0)
        informAgsEnvironmentChanged()
        return true // the action was executed with success
    }

    private fun updateAgPercept(agName: String, ag: Int) {
        clearPercepts(agName)
        // its location
        val l = model.getAgPos(ag)
        val p = ASSyntax.createLiteral(
            "pos",
            ASSyntax.createNumber(l.x.toDouble()),
            ASSyntax.createNumber(l.y.toDouble()),
            //ASSyntax.createNumber(getStep())
        )
        addPercept(agName, p)

        // what's around
        updateAgPercept(agName, ag, l.x - 1, l.y - 1)
        updateAgPercept(agName, ag, l.x - 1, l.y)
        updateAgPercept(agName, ag, l.x - 1, l.y + 1)
        updateAgPercept(agName, ag, l.x, l.y - 1)
        updateAgPercept(agName, ag, l.x, l.y)
        updateAgPercept(agName, ag, l.x, l.y + 1)
        updateAgPercept(agName, ag, l.x + 1, l.y - 1)
        updateAgPercept(agName, ag, l.x + 1, l.y)
        updateAgPercept(agName, ag, l.x + 1, l.y + 1)
    }



    private fun updateAgPercept(agName: String, agId: Int, x: Int, y: Int) {
        //if (random.nextDouble() < model.getAgFatigue(agId)) return  // perception omission
        if (model == null || !model.inGrid(x, y)) return
        if (model.hasObject(GridWorldModel.OBSTACLE, x, y)) {
            addPercept(agName, ASSyntax.createLiteral(
                "obstacle",
                ASSyntax.createNumber(x.toDouble()),
                ASSyntax.createNumber(y.toDouble()),
            ))
        }
    }

}
