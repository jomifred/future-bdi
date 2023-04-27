package example.grid

import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class GridEnvModel : EnvironmentModel<GridLocation>, GridWorldModel(30, 30, 1) {

    private val actions = listOf(
        Action("n"),
        Action("nw"),
        Action("ne"),
        Action("w"),
        Action("e"),
        Action("s"),
        Action("sw"),
        Action("se"),
    )

    override fun actions() = actions

    override fun next(s: GridLocation, a: Action): GridLocation {
        return when(a.name) {
            "n" -> GridLocation(s.x(), s.y() - 1)
            "nw"-> GridLocation(s.x() - 1, s.y() - 1)
            "ne"-> GridLocation(s.x() + 1, s.y() - 1)
            "w" -> GridLocation(s.x() - 1, s.y())
            "e" -> GridLocation(s.x() + 1, s.y())
            "s" -> GridLocation(s.x(), s.y() + 1)
            "sw"-> GridLocation(s.x() - 1, s.y() + 1)
            "se"-> GridLocation(s.x() + 1, s.y() + 1)
            else -> s
            }.let {
                if (isFree(it.x(), it.y())) it else s
            }

//        return  (if (a.name == "n")  GridLocation(s.x(), s.y() - 1)
//            else if (a.name == "nw") GridLocation(s.x() - 1, s.y() - 1)
//            else if (a.name == "ne") GridLocation(s.x() + 1, s.y() - 1)
//            else if (a.name == "w")  GridLocation(s.x() - 1, s.y())
//            else if (a.name == "e")  GridLocation(s.x() + 1, s.y())
//            else if (a.name == "s")  GridLocation(s.x(), s.y() + 1)
//            else if (a.name == "sw") GridLocation(s.x() - 1, s.y() + 1)
//            else if (a.name == "se") GridLocation(s.x() + 1, s.y() + 1)
//            else s)
//            .let {
//                if (isFree(it.x(), it.y())) it else s
//            }
//                .let { it } //if isFree(it.x, it.y) it else s }

//        var newLoc = s as GridLocation
//
//             if (a.name == "n")   newLoc = GridLocation(s.x(),s.y()-1)
//        else if (a.name == "nw")  newLoc = GridLocation(s.x()-1,s.y()-1)
//        else if (a.name == "ne")  newLoc = GridLocation(s.x()+1,s.y()-1)
//        else if (a.name == "w")   newLoc = GridLocation(s.x()-1,s.y())
//        else if (a.name == "e")   newLoc = GridLocation(s.x()+1,s.y())
//        else if (a.name == "s")   newLoc = GridLocation(s.x(),s.y()+1)
//        else if (a.name == "sw")  newLoc = GridLocation(s.x()-1,s.y()+1)
//        else if (a.name == "se")  newLoc = GridLocation(s.x()+1,s.y()+1)
//
//        if (isFree( newLoc.x(), newLoc.y()))
//            return newLoc
//        else
//            return s
    }

    init {
        addWall(10,15,20,15)
        setAgPos( 0, 15, 5)
    }
}

class GridLocation : State {
    val jgl: Location

    constructor(x: Int, y: Int) {
        jgl = Location(x,y)
    }
    constructor(l: Location) {
        jgl = l
    }

    fun x() = jgl.x
    fun y() = jgl.y

    override fun toString() = "<${jgl}>"

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is GridLocation) return jgl == other.jgl
        return false
    }

    override fun hashCode() = jgl.hashCode()

    fun distance(l: GridLocation) = jgl.distanceEuclidean(Location(l.jgl.x, l.jgl.y))
}
