package example.normative_grid

import example.grid.GridEnvView
import java.awt.Color
import java.awt.Graphics

/** class that implements the View of Grid Env */
class LTZGridEnvView(
    val lModel: LTZGridEnvModel,
    env: LTZGridJasonEnv)
    : GridEnvView(lModel, env) {

    override fun draw(g: Graphics, x: Int, y: Int, obj: Int) {
        super.draw(g, x, y, obj)
        if (obj == lModel.LT_ZONE)
            drawZone(g, x, y)
    }

    private fun drawZone(g: Graphics, x: Int, y: Int) {
        g.color = Color.yellow
        g.fillRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4)
    }
}