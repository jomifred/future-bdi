package example.bridge

import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.asSyntax.Structure
import jason.environment.grid.GridWorldModel
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.State

class BridgeEnvModel(
    private var currentState: BridgeState,
    var scenario : Int = 0
) : EnvironmentModel<BridgeState, BridgeAction>, GridWorldModel(20, 20, 2) {

    init {
        setScenarioWalls(scenario)
        setInitState(currentState)
    }

    override fun id() =
        when (scenario) {
            0 -> "bridge"
            else -> "none"
        }

    override fun hasGUI() = view != null

    fun setInitState(c: BridgeState) {
        currentState = c
        setAgPos(0, currentState.l[0])
        setAgPos(1, currentState.l[1])
    }

    fun setScenarioWalls(i: Int) {
        scenario = i
        removeAll(OBSTACLE)
        addWalls0()
    }

    fun addWalls0() {
        addWall(4, 0, 15, 7)
        addWall(4, 9, 15, height - 1)
    }

    override fun clone(): BridgeEnvModel =
        BridgeEnvModel(
            BridgeState(currentState),
            scenario
        )

    override fun actions() = emptyList<BridgeAction>()

    override fun structureToAction(agName: String, jasonAction: Structure): BridgeAction {
        return BridgeAction(agNameToNb(agName), jasonAction.functor, 1.0) // here, only functor is relevant
    }

    fun agNameToNb(agName: String) =
        if (agName.contains("r2"))
            1
        else
            0


    override fun currentState(): BridgeState = currentState

    override fun execute(a: BridgeAction): State {
        currentState = next(currentState, a)
        setAgPos(0, currentState.l[0])
        setAgPos(1, currentState.l[1])
        return currentState()
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = fixedPerception.toMutableList()
        val ag = agNameToNb(agName)
        val l = getAgPos(ag)
        p.add(
            ASSyntax.createLiteral(
                "pos",
                ASSyntax.createNumber(l.x.toDouble()),
                ASSyntax.createNumber(l.y.toDouble()),
            )
        )

        for (v in getAdjacent(l).values) {
            if (hasObject(OBSTACLE, v.x, v.y)) {
                p.add(
                    ASSyntax.createLiteral(
                        "obstacle",
                        ASSyntax.createNumber(v.x.toDouble()),
                        ASSyntax.createNumber(v.y.toDouble()),
                    )
                )
            }
            if (hasObject(AGENT, v.x, v.y) && (l.x != v.x || l.y != v.y)) {
                p.add(
                    ASSyntax.createLiteral(
                        "agent",
                        ASSyntax.createNumber(getAgAtPos(v.x,v.y).toDouble()),
                        ASSyntax.createNumber(v.x.toDouble()),
                        ASSyntax.createNumber(v.y.toDouble()),
                    )
                )
            }
        }
        return p
    }

    private val fixedPerception: List<Literal> by lazy {
        listOf<Literal>(
            ASSyntax.createLiteral(
                "w_size",
                ASSyntax.createNumber(width.toDouble()),
                ASSyntax.createNumber(height.toDouble()),
            )
        )
    }

    private fun getAdjacent(l: Location): Map<String, Location> {
        return mapOf(
            "idle" to Location(l.x, l.y),
            "n" to Location(l.x, l.y - 1),
            "nw" to Location(l.x - 1, l.y - 1),
            "ne" to Location(l.x + 1, l.y - 1),
            "w" to Location(l.x - 1, l.y),
            "e" to Location(l.x + 1, l.y),
            "s" to Location(l.x, l.y + 1),
            "sw" to Location(l.x - 1, l.y + 1),
            "se" to Location(l.x + 1, l.y + 1)
        )
    }


    override fun next(s: BridgeState, a: BridgeAction): BridgeState {
        fun Location.ifFreeOrL(l: Location): Location = if (isFree(this)) this else l

        when (a.ag)  {
            0    -> return BridgeState(getAdjacent(s.l[0]).getOrDefault(a.name, s.l[0]).ifFreeOrL(s.l[0]), s.l[1])
            else -> return BridgeState(s.l[0], getAdjacent(s.l[1]).getOrDefault(a.name, s.l[1]).ifFreeOrL(s.l[1]))
        }
    }
}

/**
 * A Brdige state is the location of agent A and location of agent B
 */
class BridgeState : State {
    val l: Array<Location>

    constructor(xa: Int, ya: Int, xb: Int, yb: Int) {
        l = arrayOf(Location(xa,ya), Location(xb,yb))
    }
    constructor(a: Location, b: Location) {
        l = arrayOf(a,b)
    }
    constructor(b: BridgeState) : this(b.l[0], b.l[1])

    override fun toString() = "<${l[0]}|${l[1]}>"

    override fun equals(other: Any?): Boolean {
        if (this === other)  return true
        if (other is BridgeState) return l[0] == other.l[0] && l[1] == other.l[1]
        return false
    }

    override fun hashCode() = l[0].hashCode() + l[1].hashCode() * 31

    /*override fun asJason() =
        ASSyntax.createLiteral("bridge_s",
            NumberTermImpl(l[0].x.toDouble()),
            NumberTermImpl(l[0].y.toDouble()),
            NumberTermImpl(l[1].x.toDouble()),
            NumberTermImpl(l[1].y.toDouble())
        )*/
}

class BridgeAction(val ag: Int, name: String, cost: Double) : Action(name, cost) {
}
