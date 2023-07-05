package example.dyngrid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.future.Action
import jason.future.EnvironmentModel

class DynamicGridEnvModel(
    currentState: GridState,
    goalState   : GridState,
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1) {

    private val wallSize = 7
    private val walls = mutableListOf<Pair<Int, Int>>()
    val pChange = 0.4

    /** certainty of the next state */
    override fun gamma() = 1.0-(pChange*0.1) // * 8/(height*width))

    override fun clone(): DynamicGridEnvModel {
        val n = DynamicGridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l)
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
        addWall(
            (1..width - wallSize - 2).random(),
            (1..height - 2).random()
        )
    }

    fun addWall(x: Int, y: Int) {
        for (i in x - 1..x + wallSize + 1)
            if (!isFree(i, y) || hasObject(DEST, i, y))
                return
        addWall(x, y, x + wallSize, y)
        walls.add(Pair(x, y))
    }

    fun remRandomWall() {
        if (walls.isEmpty()) return
        val p = walls.removeAt((0 until walls.size).random())
        for (i in p.first..p.first + wallSize) {
            remove(OBSTACLE, i, p.second)
        }
    }
}