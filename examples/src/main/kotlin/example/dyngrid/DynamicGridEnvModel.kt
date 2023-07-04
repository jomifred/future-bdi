package example.dyngrid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class DynamicGridEnvModel(
    currentState: GridState,
    goalState   : GridState,
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1) {

    val WALLSIZE = 7

    val walls = mutableListOf<Pair<Int,Int>>()

    override fun clone(): DynamicGridEnvModel {
        val n = DynamicGridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l))
        for (p in walls)
            n.addWall(p.first, p.second)
        return n
    }

    override fun execute(a: Action): State {
        return super.execute(a)
    }

    fun addRandomWall()  {
        addWall(
            (1..width-WALLSIZE-2).random(),
            (1..height-2).random())
    }

    fun addWall(x: Int, y: Int)  {
        // add or remove some wall
        addWall(x,y, x+WALLSIZE, y )
        walls.add(Pair(x,y))
    }

    /*override fun next(s: DynamicGridState, a: Action): DynamicGridState {
        fun DynamicGridState.ifFreeOrS(): DynamicGridState = if (isFree(l)) this else s

        return DynamicGridState(getAdjacent(s).getOrDefault(a.name, s).l).ifFreeOrS()
    }*/
}

//class DynamicGridState : GridState {
//    constructor(l: Location) : super(l.x,l.y)

//    override fun toString() = "<${l}>"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other)  return true
//        if (other is GridState) return l == other.l
//        return false
//    }
//
//    override fun hashCode() = l.hashCode()
//}

