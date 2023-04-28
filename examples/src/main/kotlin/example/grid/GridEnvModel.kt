package example.grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.asSyntax.Structure
import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class GridEnvModel(
    private var currentState: GridLocation
) : EnvironmentModel<GridLocation>, GridWorldModel(30, 30, 1) {

    init {
        addWall(10,15,20,15)
        setAgPos( 0, currentState.l.x, currentState.l.y)
    }

    public override fun clone(): GridEnvModel = GridEnvModel( GridLocation(currentState.l))

    private val actions = listOf(
        Action("n"),
        Action("nw"),
        Action("ne"),
        Action("w"),
        Action("e"),
        Action("s"),
        Action("sw"),
        Action("se"),
    ).associateBy { it.name }

    private val skip = Action("skip")

    override fun actions() = actions.values

    override fun structureToAction(jasonAction: Structure): Action {
        return actions.getOrDefault( jasonAction.functor, skip) // here, only functor is relevant
    }
    override fun currentState(): GridLocation = currentState

    override fun execute(a: Action): State {
        currentState = next(currentState, a)
        setAgPos( 0, currentState.l.x, currentState.l.y)
        return currentState()
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = fixedPerception.toMutableList()
        val l = getAgPos(0)
        p.add(ASSyntax.createLiteral(
            "pos",
            ASSyntax.createNumber(l.x.toDouble()),
            ASSyntax.createNumber(l.y.toDouble()),
        ))

        for ( v in getAdjacent( GridLocation(l)).values) {
            if (hasObject(OBSTACLE, v.l.x, v.l.y)) {
                p.add( ASSyntax.createLiteral(
                    "obstacle",
                    ASSyntax.createNumber(v.l.x.toDouble()),
                    ASSyntax.createNumber(v.l.y.toDouble()),
                ))
            }
        }
        return p
    }

    val fixedPerception : List<Literal> by lazy {
        listOf<Literal>(
            ASSyntax.createLiteral(
                "destination",
                ASSyntax.createNumber(15.0),
                ASSyntax.createNumber(8.0),
            ),
            ASSyntax.createLiteral(
                "w_size",
                ASSyntax.createNumber(width.toDouble()),
                ASSyntax.createNumber(height.toDouble()),
            )
        )
    }

    fun getAdjacent(s: GridLocation) : Map<String, GridLocation> {
        return mapOf(
            "n"  to GridLocation(s.l.x, s.l.y - 1),
            "nw" to GridLocation(s.l.x - 1, s.l.y - 1),
            "ne" to GridLocation(s.l.x + 1, s.l.y - 1),
            "w"  to GridLocation(s.l.x - 1, s.l.y),
            "e"  to GridLocation(s.l.x + 1, s.l.y),
            "s"  to GridLocation(s.l.x, s.l.y + 1),
            "sw" to GridLocation(s.l.x - 1, s.l.y + 1),
            "se" to GridLocation(s.l.x + 1, s.l.y + 1)
        )
    }

    override fun next(s: GridLocation, a: Action): GridLocation {
        fun GridLocation.ifFreeOrS(): GridLocation = if (isFree(l.x, l.y)) this else s

        return getAdjacent(s).getOrDefault(a.name, s).ifFreeOrS()

        /*return when(a.name) {
            "n" -> GridLocation(s.l.x, s.l.y - 1)
            "nw"-> GridLocation(s.l.x - 1, s.l.y - 1)
            "ne"-> GridLocation(s.l.x + 1, s.l.y - 1)
            "w" -> GridLocation(s.l.x - 1, s.l.y)
            "e" -> GridLocation(s.l.x + 1, s.l.y)
            "s" -> GridLocation(s.l.x, s.l.y + 1)
            "sw"-> GridLocation(s.l.x - 1, s.l.y + 1)
            "se"-> GridLocation(s.l.x + 1, s.l.y + 1)
            else -> s
            }.ifFreeOrS()*/

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

