package example.grid

import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class Grid : EnvironmentModel<GridLocation>, GridWorldModel {

    constructor() : super(30,30,1) {
        addWall(10,15,20,15)
        setAgPos( 0, 15, 5)
    }

    val actions = listOf(
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
        return  (if (a.name == "n")  GridLocation(s.x(), s.y() - 1)
            else if (a.name == "nw") GridLocation(s.x() - 1, s.y() - 1)
            else if (a.name == "ne") GridLocation(s.x() + 1, s.y() - 1)
            else if (a.name == "w")  GridLocation(s.x() - 1, s.y())
            else if (a.name == "e")  GridLocation(s.x() + 1, s.y())
            else if (a.name == "s")  GridLocation(s.x(), s.y() + 1)
            else if (a.name == "sw") GridLocation(s.x() - 1, s.y() + 1)
            else if (a.name == "se") GridLocation(s.x() + 1, s.y() + 1)
            else s)
            .let {
                if (isFree(it.x(), it.y())) it else s
            }
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
}

class GridLocation : State {
    val jgl: Location

    constructor(x: Int, y: Int) {
        jgl = Location(x,y)
    }

    fun x() = jgl.x
    fun y() = jgl.y

    override fun toString() = "<${jgl.toString()}>"

    override fun equals(l: Any?): Boolean {
        if (this === l)  return true
        if (l is GridLocation) return jgl.equals(l.jgl)
        return false
    }

    override fun hashCode() = jgl.hashCode()

    fun distance(l: GridLocation) = jgl.distanceEuclidean(Location(l.jgl.x, l.jgl.y))
}
