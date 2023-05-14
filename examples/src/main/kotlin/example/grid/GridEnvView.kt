package example.grid

import jason.environment.grid.GridWorldView
import jason.future.ExplorationStrategy
import jason.future.ForeseeProblemAgent
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Panel
import java.awt.event.ItemEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.concurrent.thread

/** class that implements the View of Grid Env */
class GridEnvView(
    val gModel: GridEnvModel,
    private val env: GridJasonEnv)
    : GridWorldView(gModel, "Future!", 800) {

    init {
        isVisible = true
        repaint()
        canvas.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                val col = e.x / cellSizeW
                val lin = e.y / cellSizeH
                if (col >= 0 && lin >= 0 && col < getModel().width && lin < getModel().height) {
                    gModel.setGoal( GridState(col, lin))
                    resetGUI()
                }
            }

            override fun mouseExited(e: MouseEvent) {}
            override fun mouseEntered(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {}
            override fun mouseReleased(e: MouseEvent) {}
        })
    }

    fun resetGUI() {
        env.updatePercept()
        ForeseeProblemAgent.clearVisited()
        ForeseeProblemAgent.setMsg("")
        model.removeAll( gModel.VISITED )
        model.removeAll( gModel.SOLUTION )
        update()
    }

    private var msgText : JLabel? = null

    override fun initComponents(width: Int) {
        super.initComponents(width)
        val strategies = JComboBox<ExplorationStrategy>()
        for (s in ForeseeProblemAgent.getImplementedStrategies())
            strategies.addItem(s)

        strategies.selectedItem = ForeseeProblemAgent.strategy()
        strategies.apply {
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    //println("select ${it.item}")
                    ForeseeProblemAgent.setStrategy(it.item as ExplorationStrategy)
                }
            }
        }

        val scenarios = JComboBox<String>()
        scenarios.addItem("--")
        scenarios.addItem("U")
        scenarios.addItem("H")
        scenarios.addItem("O")
        scenarios.apply {
            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    gModel.setScenarioWalls(scenarios.selectedIndex)
                }
                resetGUI()
            }
        }
        val confPanel = Panel(FlowLayout())
        confPanel.add(strategies)
        confPanel.add(scenarios)

        msgText = JLabel("<msg>")
        val bot = JPanel(BorderLayout())
        bot.add( BorderLayout.EAST, confPanel )
        bot.add( BorderLayout.WEST, msgText )
        contentPane.add(BorderLayout.SOUTH, bot)

        thread(start = true) {
            while (true) {
                try {
                    Thread.sleep(300)

                    msgText?.text = ForeseeProblemAgent.getMsg()
                    for (s in ForeseeProblemAgent.getVisited()) {
                        s as GridState
                        gModel.add(gModel.VISITED, s.l)
                    }
                    for (s in ForeseeProblemAgent.getSolution()) {
                        s as GridState
                        gModel.add(gModel.SOLUTION, s.l)
                    }

                    strategies.selectedItem = ForeseeProblemAgent.strategy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun draw(g: Graphics, x: Int, y: Int, obj: Int) {
        when (obj) {
            gModel.DEST -> drawDest(g, x, y)
            gModel.VISITED -> drawVisited(g, x, y)
            gModel.SOLUTION -> drawSolution(g, x, y)
            else -> { }
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

    private fun drawVisited(g: Graphics, x: Int, y: Int) {
        g.color = Color.lightGray
        g.fillRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4)
    }
    private fun drawSolution(g: Graphics, x: Int, y: Int) {
        g.color = Color.cyan
        g.fillRect(x * cellSizeW + 4, y * cellSizeH + 4, cellSizeW - 8, cellSizeH - 8)
    }
}