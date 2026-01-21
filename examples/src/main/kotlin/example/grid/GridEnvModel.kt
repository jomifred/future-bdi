package example.grid

import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.asSyntax.Structure
import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.*

open class GridEnvModel(
    protected var currentState: GridState,
    var goalState   : GridState,
    var scenario : Int =  0,
    wwidth       : Int = 30,
    wheight      : Int = 30
) : EnvironmentModel<GridState, Action>, GridWorldModel(wwidth, wheight, 1) {

    val DEST = 16 // represent the destination
    val VISITED = 32
    val SOLUTION = 64
    val LT_ZONE = 128
    val PORTAL = 256

    init {
        setScenarioWalls(scenario)
        setAgPos( 0, currentState.l)
        add( DEST, goalState.l)

        StatData.scenario = id()
    }

    override fun id() =
        when (scenario) {
            0 -> "line"
            1 -> "U"
            2 -> "H"
            3 -> "O"

            -2 -> "LTZ"
            -3 -> "LTZ_U"

            else -> "none"
        }

    override fun hasGUI() = view != null

    fun setInitState(c: GridState) {
        currentState = c
        setAgPos( 0, currentState.l)
    }
    open fun setGoal(c: GridState) {
        remove( DEST, goalState.l)
        goalState = c
        add( DEST, goalState.l)
    }

    fun setScenarioWalls(i: Int) {
        scenario = i
        removeAll(OBSTACLE)
        removeAll(LT_ZONE)
        if (scenario >= 0) addWalls0()
        if (scenario >  0) addWalls1()
        if (scenario >  1) addWalls2()
        if (scenario >  2) addWalls3()
        if (scenario == -2) addLTZ()
        if (scenario == -3) addLTZU()
    }
    fun addWalls0() {
        addWall(12,15,20,15)
    }
    fun addWalls1() {
        addWall(12,7, 12, 15 )
        addWall(20,7, 20, 15 )
    }
    fun addWalls2() {
        addWall(12,19,20,19)
        addWall(12,19, 12, 23 )
        addWall(20,19, 20, 23 )
    }
    fun addWalls3() {
        addWall(12,23,20,23)
    }

    fun addLTZ() {
        for (x in 8..20)
            for (y in 6..12)
                add(LT_ZONE, x, y)
        for (x in 14..17)
            for (y in 13..15)
                add(LT_ZONE, x, y)
        for (x in 6..19)
            for (y in 16..19)
                add(LT_ZONE, x, y)
    }

    fun addLTZU() {
        for (x in 7..20)
            add(LT_ZONE,x,15)
        for (y in 7..15 ) {
            add(LT_ZONE,7,y)
            add(LT_ZONE,20,y)
        }
    }

    override fun clone(): GridEnvModel =
        GridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l),
            scenario
        )

    private val actions = listOf(
        Action("n", 1.0),
        Action("nw",1.4),
        Action("ne",1.4),
        Action("w", 1.0),
        Action("e", 1.0),
        Action("s", 1.0),
        Action("sw",1.4),
        Action("se",1.4),
        Action("idle", 0.9)
    ).associateBy { it.name }

    private val skip = Action("idle", 0.9)

    override fun actions() = actions.values

    override fun structureToAction(agName: String, jasonAction: Structure): Action {
        return actions.getOrDefault( jasonAction.functor, skip) // here, only functor is relevant
    }

    override fun currentState(): GridState = currentState

    override fun execute(a: Action): State {
        currentState = next(currentState, a)
        setAgPos( 0, currentState.l)
        return currentState
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = fixedPerception.toMutableList()
        val l = getAgPos(0)
        p.add(ASSyntax.createLiteral(
            "pos",
            ASSyntax.createNumber(l.x.toDouble()),
            ASSyntax.createNumber(l.y.toDouble()),
        ))

        for ( v in getAdjacent(GridState(l)).values) {
            if (hasObject(OBSTACLE, v.l.x, v.l.y)) {
                p.add( ASSyntax.createLiteral(
                    "obstacle",
                    ASSyntax.createNumber(v.l.x.toDouble()),
                    ASSyntax.createNumber(v.l.y.toDouble()),
                ))
            }
        }

        p.add(ASSyntax.createLiteral(
            "destination",
            ASSyntax.createNumber(goalState.l.x.toDouble()),
            ASSyntax.createNumber(goalState.l.y.toDouble()),
        ))

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (hasObject(LT_ZONE, x, y)) {
                    p.add(
                        ASSyntax.createLiteral(
                            "ltz",
                            ASSyntax.createNumber(x.toDouble()),
                            ASSyntax.createNumber(y.toDouble()),
                        )
                    )
                }
            }
        }

        return p
    }

    protected val fixedPerception : List<Literal> by lazy {
        listOf<Literal>(
            ASSyntax.createLiteral(
                "w_size",
                ASSyntax.createNumber(width.toDouble()),
                ASSyntax.createNumber(height.toDouble()),
            )
        )
    }

    fun getAdjacent(s: GridState) : Map<String, GridState> {
        return mapOf(
            "n"  to GridState(s.l.x, s.l.y - 1),
            "nw" to GridState(s.l.x - 1, s.l.y - 1),
            "ne" to GridState(s.l.x + 1, s.l.y - 1),
            "w"  to GridState(s.l.x - 1, s.l.y),
            "e"  to GridState(s.l.x + 1, s.l.y),
            "s"  to GridState(s.l.x, s.l.y + 1),
            "sw" to GridState(s.l.x - 1, s.l.y + 1),
            "se" to GridState(s.l.x + 1, s.l.y + 1)
        )
    }

    override fun next(s: GridState, a: Action): GridState {
        fun GridState.ifFreeOrS(): GridState = if (isFree(l)) this else s

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

open class GridState : State {
    val l: Location // jason grid location

    constructor(x: Int, y: Int) {
        l = Location(x,y)
    }
    constructor(l: Location) : this(l.x,l.y)

    override fun toString() = "<${l}>"

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is GridState) return l == other.l
        return false
    }

    override fun hashCode() = l.hashCode()

    fun distance(l: GridState) = this.l.distanceEuclidean(Location(l.l.x, l.l.y))

    /*override fun asJason() =
        ASSyntax.createLiteral("pos",
            NumberTermImpl(l.x.toDouble()),
            NumberTermImpl(l.y.toDouble()))*/
}

