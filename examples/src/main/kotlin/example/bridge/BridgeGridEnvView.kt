package example.bridge

import jason.environment.grid.GridWorldView
import jason.future.Search

/** class that implements the View of Bridge Env */
class BridgeGridEnvView(
    val gModel: BridgeEnvModel,
    private val env: BridgeJasonEnv)
    : GridWorldView(gModel, "Future!", 600) {

    init {
        isVisible = true
        repaint()
    }

    fun resetGUI() {
        env.updatePercept()
        Search.clearVisited()
        Search.setMsg("")
        update()
    }
}