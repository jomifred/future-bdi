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
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1, 30,30) {

    private var step : Int = 0
    private var visited = ArrayList<Location>()
    private val portal = Location(18,8)

    override fun id() = "ltz-grid"

    init {
        ForeseeProblemAgent.data.scenario = id()
        addLTZ()
        add(PORTAL, portal)
    }

    override fun execute(a: Action): State {
        step++
        visited.add(currentState.l)
        return super.execute(a)
    }

    override fun setGoal(c: GridState) {
        super.setGoal(c)
        visited.clear()
    }

    override fun clone(): LTZGridEnvModel {
        return LTZGridEnvModel(
            GridState(currentState.l),
            GridState(goalState.l),
        )
    }

    override fun agPerception(agName: String): MutableCollection<Literal> {
        val p = super.agPerception(agName)

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
        p.add(ASSyntax.createLiteral("portal",
            ASSyntax.createNumber(portal.x.toDouble()),
            ASSyntax.createNumber(portal.y.toDouble())))

        for (g in visited) {
            p.add(ASSyntax.createLiteral(
                "visited",
                ASSyntax.createNumber(g.x.toDouble()),
                ASSyntax.createNumber(g.y.toDouble()),
            ))
        }
        return p
    }

    private fun addLTZ()  {
        for (x in 10..20)
            for (y in 9..15)
                add(LT_ZONE,x,y)
        for (x in 13..17)
            for (y in 16..19)
                add(LT_ZONE,x,y)
        for (x in 9..19)
            for (y in 20..22)
                add(LT_ZONE,x,y)
    }

}