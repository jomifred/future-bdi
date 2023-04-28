package jason.agent

import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Literal

/** Agent class that select options based on preference */
open class PreferenceAgent : Agent() {

    override fun selectOption(options: MutableList<Option>): Option? =
        options
            .associateWith {
                it.plan.label
                    .annots
                    .filter { it.isLiteral && it.toString().startsWith("preference") }
            }
            .filterValues { it.isNotEmpty() }
            .minByOrNull { (it.value.first().capply(it.key.unifier) as Literal).getTerm(0) }
            ?. key
            ?: super.selectOption(options)
}