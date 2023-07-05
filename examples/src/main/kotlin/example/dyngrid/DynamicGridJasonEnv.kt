package example.dyngrid

// Environment code for project grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.asSyntax.Structure
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.MatrixCapable
import kotlin.random.Random

class DynamicGridJasonEnv : GridJasonEnv(), MatrixCapable<GridState, Action> {

    private val nbInitialWalls = 6

    init {
        model = DynamicGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17)  // default goal  state
        )

        for (i in 0 until nbInitialWalls)
            (model as DynamicGridEnvModel).addRandomWall()
        //(model as DynamicGridEnvModel).addWall(12,15)
    }
    override fun getModel(): EnvironmentModel<GridState, Action> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        val dmodel = model as DynamicGridEnvModel

        // test if action moves to a free location and produce action failure if not
        val a = dmodel.structureToAction(agName, action)
        val s = dmodel.currentState()
        if (!dmodel.isFree( dmodel.getAdjacent(s).getOrDefault(a.name, s).l )) {
            log.info("*** error trying action ${action} (${a.name}) from ${dmodel.currentState()} to a non free location ${dmodel.getAdjacent(s).getOrDefault(a.name, s).l}")
            return false
        }

        val r = super.executeAction(agName, action)

        // add or remove some wall (after the agent move)
        if (Random.nextDouble() > 0.5) {
            if (Random.nextDouble() > dmodel.pChange)
                dmodel.addRandomWall()
        } else {
            if (Random.nextDouble() > dmodel.pChange)
                dmodel.remRandomWall()
        }
        //view?.resetGUI()

        return r
    }
}
