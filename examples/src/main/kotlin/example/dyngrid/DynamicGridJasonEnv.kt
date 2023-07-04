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

    private val nbInitialWalls = 5
    private val gamma = 0.4

    init {
        model = DynamicGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17)  // default goal  state
        )

        for (i in 0 until nbInitialWalls)
            (model as DynamicGridEnvModel).addRandomWall()

    }
    override fun getModel(): EnvironmentModel<GridState, Action> = model

    override fun executeAction(agName: String, action: Structure): Boolean {

        // add or remove some wall
        val dmodel = model as DynamicGridEnvModel
        if (Random.nextDouble() > 0.5) {
            if (Random.nextDouble() > gamma)
                dmodel.addRandomWall()
        } else {
            if (Random.nextDouble() > gamma)
                dmodel.remRandomWall()
        }
        view?.resetGUI()

        // test if action moves to a free location and produce action failure if not
        val a = dmodel.structureToAction(agName, action)
        val s = dmodel.currentState()
        if (!dmodel.isFree( dmodel.getAdjacent(s).getOrDefault(a.name, s).l ))
            return false

        return super.executeAction(agName, action)
    }
}
