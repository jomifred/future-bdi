package example.dyngrid

// Environment code for project grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.asSyntax.Structure
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.MatrixCapable

class DynamicGridJasonEnv : GridJasonEnv(), MatrixCapable<GridState, Action> {
    init {
        model = DynamicGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17)  // default goal  state
        )
    }
    override fun getModel(): EnvironmentModel<GridState, Action> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        // add or remove some wall
        val dmodel = model as DynamicGridEnvModel
        dmodel.addRandomWall()
        view?.resetGUI()

        // test if action was possible
        val a = dmodel.structureToAction(agName, action)
        val s = dmodel.currentState()
        if (!dmodel.isFree( dmodel.getAdjacent(s).getOrDefault(a.name, s).l ))
            return false

        return super.executeAction(agName, action)
    }
}
