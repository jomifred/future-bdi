package example.dyngrid

// Environment code for project grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.asSyntax.Structure
import jason.environment.grid.GridWorldModel.OBSTACLE
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.ForeseeProblemAgent
import jason.future.MatrixCapable
import java.io.FileReader
import java.util.*
import kotlin.random.Random

class DynamicGridJasonEnv : GridJasonEnv(), MatrixCapable<GridState, Action> {

    private val nbInitialWalls = 10

    init {
        val conf = Properties()
        var pChange = 0.4
        try {
            conf.load(FileReader("params.properties"))
            pChange = conf.getOrDefault("pChange", pChange).toString().toDouble()

            /*val maxTime = conf.getOrDefault("maxTime", 0).toString().toLong()
            if (maxTime>0) {
                thread(start = true) {
                    Thread.sleep(maxTime)
                    log.info("***** stop by time out *****")
                    System.exit(0)
                }
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        model = DynamicGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17),  // default goal  state
            pChange
        )

        for (i in 0 until nbInitialWalls)
            (model as DynamicGridEnvModel).addRandomWall()
        //(model as DynamicGridEnvModel).addWall(12,15)
    }
    override fun getModel(): EnvironmentModel<GridState, Action> = model

    override fun executeAction(agName: String, action: Structure): Boolean {
        val dmodel = model as DynamicGridEnvModel
        ForeseeProblemAgent.data.nbActions++ // stats

        // test if action moves to a free location and produce action failure if not
        val a = dmodel.structureToAction(agName, action)
        val s = dmodel.currentState()
        if (dmodel.hasObject(OBSTACLE, dmodel.getAdjacent(s).getOrDefault(a.name, s).l )) {
            log.info("*** error trying action $action (${a.name}) from ${dmodel.currentState()} to a non free location ${dmodel.getAdjacent(s).getOrDefault(a.name, s).l}")
            return false
        }

        val r = super.executeAction(agName, action)

        // add or remove some wall (after the agent move)
        var change = false
        if (Random.nextDouble() >= 0.5) {
            if (Random.nextDouble() < dmodel.pChange) {
                dmodel.addRandomWall()
                change = true
            }
        } else {
            if (Random.nextDouble() < dmodel.pChange) {
                dmodel.remRandomWall()
                change = true
            }
        }
        if (change) {
            updateAgPercept(agName)
            informAgsEnvironmentChanged()
        }

        return r

        //view?.resetGUI()

    }
}
