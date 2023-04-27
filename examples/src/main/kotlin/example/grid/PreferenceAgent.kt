package example.grid

import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Literal
import jason.asSyntax.NumberTerm

class PreferenceAgent : Agent() {

    override fun selectOption(options: MutableList<Option>): Option =
        options
            .associateWith {
                it.plan.label.capplyAnnots(it.unifier)
                    .filter { it.isLiteral && it.toString().startsWith("preference") }
            }
            .filterValues { it.isNotEmpty() }
            .minByOrNull { ((it.value.first() as Literal).getTerm(0) as NumberTerm).solve() }
            ?. key
            ?: super.selectOption(options)
}