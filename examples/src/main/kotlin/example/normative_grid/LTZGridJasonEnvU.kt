package example.normative_grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.future.Action
import jason.future.ExplorationStrategy
import jason.future.MatrixCapable
import jason.environment.grid.Location

class LTZGridJasonEnvU : GridJasonEnv(), MatrixCapable<GridState, Action> {
    init {
        model = LTZGridEnvModelU(
            LTZState(Location(15, 5), 0), // default initial state
            LTZState(Location(15,17), 1000),
            -2
        )
    }
}
