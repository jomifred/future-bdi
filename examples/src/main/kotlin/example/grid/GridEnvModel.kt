package example.grid

import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class GridEnvModel : EnvironmentModel<GridLocation>, GridWorldModel(30, 30, 1) {

    init {
        addWall(10,15,20,15)
        setAgPos( 0, 15, 5)
    }

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
        fun GridLocation.ifFreeOrS(): GridLocation = if (isFree(l.x, l.y)) this else s

        return when(a.name) {
            "n" -> GridLocation(s.l.x, s.l.y - 1)
            "nw"-> GridLocation(s.l.x - 1, s.l.y - 1)
            "ne"-> GridLocation(s.l.x + 1, s.l.y - 1)
            "w" -> GridLocation(s.l.x - 1, s.l.y)
            "e" -> GridLocation(s.l.x + 1, s.l.y)
            "s" -> GridLocation(s.l.x, s.l.y + 1)
            "sw"-> GridLocation(s.l.x - 1, s.l.y + 1)
            "se"-> GridLocation(s.l.x + 1, s.l.y + 1)
            else -> s
            }.ifFreeOrS()

//        return  (if (a.name == "n")  GridLocation(s.l.x, s.l.y - 1)
//            else if (a.name == "nw") GridLocation(s.l.x - 1, s.l.y - 1)
//            else if (a.name == "ne") GridLocation(s.l.x + 1, s.l.y - 1)
//            else if (a.name == "w")  GridLocation(s.l.x - 1, s.l.y)
//            else if (a.name == "e")  GridLocation(s.l.x + 1, s.l.y)
//            else if (a.name == "s")  GridLocation(s.l.x, s.l.y + 1)
//            else if (a.name == "sw") GridLocation(s.l.x - 1, s.l.y + 1)
//            else if (a.name == "se") GridLocation(s.l.x + 1, s.l.y + 1)
//            else s)
//            .let {
//                if (isFree(it.x(), it.y())) it else s
//            }
//                .let { it } //if isFree(it.x, it.y) it else s }

//        var newLoc = s as GridLocation
//
//             if (a.name == "n")   newLoc = GridLocation(s.l.x,s.l.y-1)
//        else if (a.name == "nw")  newLoc = GridLocation(s.l.x-1,s.l.y-1)
//        else if (a.name == "ne")  newLoc = GridLocation(s.l.x+1,s.l.y-1)
//        else if (a.name == "w")   newLoc = GridLocation(s.l.x-1,s.l.y)
//        else if (a.name == "e")   newLoc = GridLocation(s.l.x+1,s.l.y)
//        else if (a.name == "s")   newLoc = GridLocation(s.l.x,s.l.y+1)
//        else if (a.name == "sw")  newLoc = GridLocation(s.l.x-1,s.l.y+1)
//        else if (a.name == "se")  newLoc = GridLocation(s.l.x+1,s.l.y+1)
//
//        if (isFree( newLoc.x(), newLoc.y()))
//            return newLoc
//        else
//            return s
    }
}

class GridLocation : State {
    val l: Location // jason grid location

    constructor(x: Int, y: Int) {
        l = Location(x,y)
    }
    constructor(l: Location) {
        this.l = l
    }

    override fun toString() = "<${l}>"

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is GridLocation) return l == other.l
        return false
    }

    override fun hashCode() = l.hashCode()

    fun distance(l: GridLocation) = this.l.distanceEuclidean(Location(l.l.x, l.l.y))
}

