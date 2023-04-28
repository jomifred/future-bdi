package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Option

/** agent that considers the future */
class ForeseeProblemAgent : PreferenceAgent() {

    override fun selectOption(options: MutableList<Option>): Option {
        val defaultOption = super.selectOption(options)

        // simulates the future of default options


        return defaultOption
    }

}