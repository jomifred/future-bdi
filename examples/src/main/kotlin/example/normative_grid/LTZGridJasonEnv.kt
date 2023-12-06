package example.normative_grid

import example.grid.GridJasonEnv
import example.grid.GridState
import jason.future.Action
import jason.future.ExplorationStrategy
import jason.future.MatrixCapable

class LTZGridJasonEnv : GridJasonEnv(), MatrixCapable<GridState, Action> {

    init {
        model = LTZGridEnvModel(
            GridState(15, 5), // default initial state
            GridState(15,17),  // default goal  state
        )
    }

    override fun init(args: Array<String>?) {
        super.init(args)
        delay = 100
        setStrategy(ExplorationStrategy.SOLVE_M)
    }

}
