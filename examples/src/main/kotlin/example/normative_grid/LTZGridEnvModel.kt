package example.normative_grid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.environment.grid.Location
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.ForeseeProblemAgent
import jason.future.State

class LTZGridEnvModel(
    currentState: GridState,
    goalState   : GridState,
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1, 25,25) {

    private var step : Int = 0
    private var visited = mutableListOf<Location>()
    private val portals = mutableListOf<Location>()

    override fun id() = "ltz-grid"

    init {
        ForeseeProblemAgent.expData.scenario = id()
        addLTZ()
        portals.add(Location(18,5))
        portals.add(Location(3,21))
        for (p in portals)
            add(PORTAL, p)
        visited.add(currentState.l)
    }

    override fun execute(a: Action): State {
        step++
        val r = super.execute(a)
        visited.add(currentState.l)
        return r
    }

    override fun setGoal(c: GridState) {
        super.setGoal(c)
        visited.clear()
    }

    override fun clone(): LTZGridEnvModel {
        val r =LTZGridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l),
        )
        r.step = this.step
        r.visited.addAll(this.visited)
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

    private fun addLTZ()  {
        for (x in 8..20)
            for (y in 6..12)
                add(LT_ZONE,x,y)
        for (x in 14..17)
            for (y in 13..15)
                add(LT_ZONE,x,y)
        for (x in 6..19)
            for (y in 16..19)
                add(LT_ZONE,x,y)
    }

}