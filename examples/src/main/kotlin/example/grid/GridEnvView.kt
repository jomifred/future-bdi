package example.grid

import jason.environment.grid.GridWorldView
import jason.future.ExplorationStrategy
import jason.future.ForeseeProblemAgent
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.event.ItemEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.concurrent.thread

/** class that implements the View of Grid Env */
class GridEnvView(model: GridEnvModel, env: GridJasonEnv) : GridWorldView(model, "Future!", 800) {
    private var gModel: GridEnvModel

    init {
        gModel = model
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

    private var msgText : JLabel? = null

    override fun initComponents(width: Int) {
        super.initComponents(width)
        val scenarios = JComboBox<ExplorationStrategy>()
        for (s in ExplorationStrategy.values())
            scenarios.addItem(s)
        scenarios.selectedItem = ForeseeProblemAgent.defaultStrategy()
        scenarios.apply {
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    //println("select ${it.item}")
                    ForeseeProblemAgent.setStrategy(it.item as ExplorationStrategy)
                }
            }
        }

        msgText = JLabel("<msg>")
        val bot = JPanel(BorderLayout())
        bot.add( BorderLayout.EAST, scenarios )
        bot.add( BorderLayout.WEST, msgText )
        thread(start = true) {
            while (true) {
                msgText?.text = ForeseeProblemAgent.getMsg()
                Thread.sleep(500)
            }
        }
        contentPane.add(BorderLayout.SOUTH, bot)
    }

    override fun draw(g: Graphics, x: Int, y: Int, obj: Int) {
        when (obj) {
            gModel.DEST -> drawDest(g, x, y)
            else -> super.draw(g, x, y, obj)
        }
    }

    private fun drawDest(g: Graphics, x: Int, y: Int) {
        g.color = Color.gray
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH)
        g.color = Color.pink
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4)
        g.drawLine(x * cellSizeW + 2, y * cellSizeH + 2, (x + 1) * cellSizeW - 2, (y + 1) * cellSizeH - 2)
        g.drawLine(x * cellSizeW + 2, (y + 1) * cellSizeH - 2, (x + 1) * cellSizeW - 2, y * cellSizeH + 2)
    }

}