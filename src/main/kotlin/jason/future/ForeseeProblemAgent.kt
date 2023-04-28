package jason.future

import jason.agent.PreferenceAgent
import jason.asSemantics.Event
import jason.asSemantics.Option
import jason.infra.local.RunLocalMAS

/** agent that considers the future */
class ForeseeProblemAgent : PreferenceAgent() {

    private var inMatrix = false

    override fun selectOption(options: MutableList<Option>): Option {
        val defaultOption = super.selectOption(options)

        if (inMatrix)
            // do not consider the future in matrix mode
            return defaultOption


        // simulates the future of default options

        // clone environment model (based on current environment)
        val userEnv = RunLocalMAS.getRunner().environmentInfraTier.userEnvironment
        if (userEnv is MatrixCapable<*>) {
            val envModel = userEnv.getModel().clone()
            //println("env $envModel ${RunLocalMAS.getRunner().environmentInfraTier.userEnvironment}")

            // clone agent model (based on this agent)
            val agArch = MatrixAgentArch(
                envModel as EnvironmentModel<State>,
                "${ts.agArch.agName}_matrix")
            val agModel = clone(agArch) as ForeseeProblemAgent
            agModel.inMatrix = true
            agModel.ts.setLogger(agArch)

            println("starting simulation...")
            // run agent until an action
            val evt = ts.c.selectedEvent.clone() as Event
            if (evt?.intention != null) {
                // add event / current option in the clone, so it continues from here
                evt.option = defaultOption
                agModel.ts.c.addEvent(evt)
                agArch.run(evt)
            }
        }

        return defaultOption
    }

}