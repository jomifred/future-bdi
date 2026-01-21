package example.normative_grid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.environment.grid.Location
import jason.future.*

// LTZ state considers the step as part of the state, so that idle "changes" the state

open class LTZState(l: Location, val step: Int) : GridState(l) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LTZState) return false
        if (l != other.l) return false
        if (step != other.step) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode() + step.hashCode()
    }
}

open class LTZGridEnvModel(
    currentState: LTZState,
    goalState   : LTZState,
    scenario    : Int
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, scenario, 25,25) {

    private var step : Int = 0
    private var visited = mutableListOf<Location>()
    protected val portals = mutableListOf<Location>()

    init {
        //StatData.scenario = id()
        //addLTZ()
        //addLTZU()
        /*portals.add(Location(18,5))
        portals.add(Location(3,21))
        for (p in portals)
            add(PORTAL, p)*/
        visited.add(currentState.l)
    }

    fun addPortal(x: Int, y: Int) {
        val p = Location(x,y)
        portals.add(p)
        add(PORTAL, p)
    }

    override fun execute(a: Action): State {
        val r = super.execute(a)
        visited.add(currentState.l)
        return r
    }

    override fun setGoal(c: GridState) {
        super.setGoal(c)
        visited.clear()
    }

    override fun next(s: GridState, a: Action): GridState {
        val n = super.next(s, a)
        step++
        return LTZState(n.l, step)
    }

    override fun clone(): LTZGridEnvModel {
        val r = LTZGridEnvModel(
            LTZState(currentState.l, step),
            LTZState(goalState.l, step),
            scenario
        )
        r.step = this.step
        r.visited.addAll(this.visited)
        r.portals.addAll(this.portals)
        return r
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = super.agPerception(agName)

        val l = getAgPos(0)
        p.add(ASSyntax.createLiteral(
            "pos",
                ASSyntax.createAtom(agName),
                ASSyntax.createNumber(l.x.toDouble()),
                ASSyntax.createNumber(l.y.toDouble()),
        ))

        p.add(ASSyntax.createLiteral("step", ASSyntax.createNumber(step.toDouble())))

        for (portal in portals) {
            p.add(
                ASSyntax.createLiteral(
                    "portal",
                    ASSyntax.createNumber(portal.x.toDouble()),
                    ASSyntax.createNumber(portal.y.toDouble())
                )
            )
        }

        for (g in visited) {
            p.add(ASSyntax.createLiteral(
                "visited",
                    ASSyntax.createAtom(agName),
                    ASSyntax.createNumber(g.x.toDouble()),
                    ASSyntax.createNumber(g.y.toDouble()),
            ))
        }
        return p
    }
}