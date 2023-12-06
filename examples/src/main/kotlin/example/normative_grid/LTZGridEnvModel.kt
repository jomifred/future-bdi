package example.normative_grid

import example.grid.GridEnvModel
import example.grid.GridState
import jason.asSyntax.ASSyntax
import jason.asSyntax.Literal
import jason.future.Action
import jason.future.EnvironmentModel
import jason.future.ForeseeProblemAgent

class LTZGridEnvModel(
    currentState: GridState,
    goalState   : GridState,
) : EnvironmentModel<GridState, Action>, GridEnvModel(currentState, goalState, -1, 30,30) {

    override fun id() = "ltz-grid"

    init {
        ForeseeProblemAgent.data.scenario = id()
        addLTZ()
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