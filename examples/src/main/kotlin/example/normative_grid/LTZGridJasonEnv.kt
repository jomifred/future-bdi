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
        var hasPortals = false
        if (!args.isNullOrEmpty()) {
            for (a in args) {
                if (a.startsWith("portals")) {
                    hasPortals = true
                }
            }
        }
        if (hasPortals) {
            (model as LTZGridEnvModel).addPortal(18,5)
            (model as LTZGridEnvModel).addPortal(3,21)
        }
        super.init(args)
        delay = 100
        setStrategy(ExplorationStrategy.SOLVE_M)
    }

}
