package example.dyngrid

// Environment code for project grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.StringTerm
import jason.asSyntax.Structure
import jason.environment.grid.GridWorldModel.OBSTACLE
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.MatrixCapable
import java.io.FileReader
import java.util.*
import kotlin.random.Random

class DynamicGridJasonEnv : GridJasonEnv(), MatrixCapable<GridState, Action> {

    private val nbInitialWalls = 5

    override fun init(args: Array<String>?) {
        var pChange = 0.4

        if (!args.isNullOrEmpty()) {
            for (a in args) {
                if (a.startsWith("params")) {
                    try {
                        val l = ASSyntax.parseLiteral(a)
                        val fileName = l.getTerm(0) as StringTerm

                        val conf = Properties()
                        conf.load(FileReader(fileName.string))
                        pChange = conf.getOrDefault("pChange", pChange).toString().toDouble()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        model = DynamicGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17),  // default goal  state
            pChange
        )

        for (i in 0 until nbInitialWalls)
            (model as DynamicGridEnvModel).addRandomWall()
        //(model as DynamicGridEnvModel).addWall(12,15)

        super.init(args)

    }
    override fun getModel(): EnvironmentModel<GridState, Action> = model

    var lastAdd = true

    override fun executeAction(agName: String, action: Structure): Boolean {
        val dmodel = model as DynamicGridEnvModel

        // test if action moves to a free location and produce action failure if not
        val a = dmodel.structureToAction(agName, action)
        val s = dmodel.currentState()

        // done in super
        //ForeseeProblemAgent.data.nbActions++ // stats
        //ForeseeProblemAgent.data.actionsCost += a.cost

        if (dmodel.hasObject(OBSTACLE, dmodel.getAdjacent(s).getOrDefault(a.name, s).l )) {
            log.info("*** error trying action $action (${a.name}) from ${dmodel.currentState()} to a non free location ${dmodel.getAdjacent(s).getOrDefault(a.name, s).l}")
            return false
        }

        val r = super.executeAction(agName, action)

        // add or remove some wall (after the agent move)
        var change = false
        if (Random.nextDouble() < dmodel.pChange) {
            if (lastAdd)
                dmodel.remRandomWall()
            else
                dmodel.addRandomWall()
            change = true
            lastAdd = !lastAdd
        }
        /*if (Random.nextDouble() >= 0.5) {
            if (Random.nextDouble() < dmodel.pChange) {
                dmodel.addRandomWall()
                change = true
            }
        } else {
            if (Random.nextDouble() < dmodel.pChange) {
                dmodel.remRandomWall()
                change = true
            }
        }*/
        if (change) {
            updateAgPercept(agName)
            informAgsEnvironmentChanged()
        }

        return r

        //view?.resetGUI()

    }
}
