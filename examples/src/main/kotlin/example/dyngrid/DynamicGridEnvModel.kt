package example.dyngrid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.ForeseeProblemAgent

class DynamicGridEnvModel(
    currentState: GridState,
    goalState   : GridState,
    val pChange     : Double
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1) {

    private val wallSize = 7
    internal val walls = mutableListOf<Pair<Int, Int>>()

    /** certainty of the next state */
    override fun gamma() = 1.0-(pChange*0.2) // * 8/(height*width))

    override fun id() = "dyngrid"

    init {
        ForeseeProblemAgent.data.scenario = id()
        ForeseeProblemAgent.data.gamma = gamma()
        ForeseeProblemAgent.data.pChange = pChange
    }

    override fun clone(): DynamicGridEnvModel {
        val n = DynamicGridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l),
            pChange
        )
        for (p in walls)
            n.addWall(p.first, p.second)
        return n
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = super.agPerception(agName)

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (hasObject(OBSTACLE, x, y)) {
                    p.add(
                        ASSyntax.createLiteral(
                            "obstacle",
                            ASSyntax.createNumber(x.toDouble()),
                            ASSyntax.createNumber(y.toDouble()),
                        )
                    )
                }
            }
        }
        return p
    }

    fun addRandomWall() {
        while (!addWall(
            (1..width - wallSize - 2).random(),
            (1..height - 2).random()
        )) {}
    }

    fun addWall(x: Int, y: Int) : Boolean {
        for (i in x - 1..x + wallSize + 1)
            if (!isFree(i, y) || hasObject(DEST, i, y))
                return false
        addWall(x, y, x + wallSize, y)
        walls.add(Pair(x, y))
        return true
    }

    fun remRandomWall() {
        if (walls.isEmpty()) return
        val p = walls.removeAt((0 until walls.size).random())
        for (i in p.first..p.first + wallSize) {
            remove(OBSTACLE, i, p.second)
        }
    }
}