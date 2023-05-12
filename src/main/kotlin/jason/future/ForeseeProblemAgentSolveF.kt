package jason.future

import jason.asSemantics.Option
import java.util.concurrent.LinkedBlockingDeque

/** agent that considers the future using strategy SOLVE_F */
open class ForeseeProblemAgentSolveF : ForeseeProblemAgent() {

    init {
        getImplementedStrategies().add(ExplorationStrategy.SOLVE_F)
        getImplementedStrategies().remove(ExplorationStrategy.SOLVE_P)
        getImplementedStrategies().remove(ExplorationStrategy.LEVEL1)
    }

    private val explorationQueueF = LinkedBlockingDeque<FutureOption>()

    override fun optionsCfParameter(options: MutableList<Option>) : List<Option> =
        super.sortedOptions(options, false)

    override fun addToExplore(fo: FutureOption) {
        val currentFO = inQueueOptions.getOrDefault(fo.getPairId(), Double.MAX_VALUE)
        if (fo.eval() < currentFO) { // if the new option is better (or new), add to explore
            inQueueOptions[fo.getPairId()] = fo.eval()
            explorationQueueF.offerFirst(fo)
        }
    }
    override fun getToExplore() : FutureOption? = explorationQueueF.poll()

    override fun selectOption(options: MutableList<Option>): Option? {
        try {
            return super.selectOption(options)
        } finally {
            explorationQueueF.clear()
        }
    }
}
