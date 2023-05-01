package example.grid

import jason.environment.grid.GridWorldView
import jason.future.ForeseeProblemAgent
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.event.ItemEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComboBox
import javax.swing.JPanel

/** class that implements the View of Grid Env */
class GridEnvView(model: GridEnvModel, env: GridJasonEnv) : GridWorldView(model, "Future!", 800) {
    var hmodel: GridEnvModel

    init {
        hmodel = model
        isVisible = true
        repaint()
        canvas.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                val col = e.x / cellSizeW
                val lin = e.y / cellSizeH
                if (col >= 0 && lin >= 0 && col < getModel().width && lin < getModel().height) {
                    model.setGoal( GridState(col, lin))
                    env.updatePercept()
                    update(col, lin)
                }
            }

            override fun mouseExited(e: MouseEvent) {}
            override fun mouseEntered(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {}
            override fun mouseReleased(e: MouseEvent) {}
        })
    }

    override fun initComponents(width: Int) {
        super.initComponents(width)
        val scenarios = JComboBox<ForeseeProblemAgent.Exploration>()
        for (s in ForeseeProblemAgent.Exploration.values())
            scenarios.addItem(s)
        scenarios.selectedItem = ForeseeProblemAgent.defaultStrategy()
        scenarios.apply {
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    //println("select ${it.item}")
                    ForeseeProblemAgent.setStrategy(it.item as ForeseeProblemAgent.Exploration)
                }
            }
        }
        val bot = JPanel(FlowLayout())
        bot.add( scenarios)
        getContentPane().add(BorderLayout.SOUTH, bot)
    }

    override fun draw(g: Graphics, x: Int, y: Int, obj: Int) {
        when (obj) {
            hmodel.DEST -> drawDest(g, x, y)
            else -> super.draw(g, x, y, obj)
        }
    }

    fun drawDest(g: Graphics, x: Int, y: Int) {
        g.color = Color.gray
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH)
        g.color = Color.pink
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4)
        g.drawLine(x * cellSizeW + 2, y * cellSizeH + 2, (x + 1) * cellSizeW - 2, (y + 1) * cellSizeH - 2)
        g.drawLine(x * cellSizeW + 2, (y + 1) * cellSizeH - 2, (x + 1) * cellSizeW - 2, y * cellSizeH + 2)
    }

}