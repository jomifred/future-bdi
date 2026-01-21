package example.normative_grid

import example.grid.GridState
import jason.environment.grid.Location
import jason.future.Action

// LTZ U considers the step as part of the state, so that idle "changes" the state

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

open class LTZGridEnvModelU(
    currentState: LTZState,
    goalState   : LTZState,
    scenario    : Int
) : LTZGridEnvModel(currentState, goalState, scenario) {

    override fun next(s: GridState, a: Action): GridState {
        val n = super.next(s, a)
        step++
        return LTZState(n.l, step)
    }

    override fun clone(): LTZGridEnvModel {
        val r = LTZGridEnvModelU(
            LTZState(currentState.l, step),
            LTZState(goalState.l, step),
            scenario
        )
        r.step = this.step
        r.visited.addAll(this.visited)
        r.portals.addAll(this.portals)
        return r
    }
}