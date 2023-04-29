package jason.agent

import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Literal
import jason.asSyntax.NumberTerm

/** Agent class that select options based on preference */
open class PreferenceAgent : Agent() {

    fun optionPrefs(options: MutableList<Option>) : Map<Option, Double> =
        options
            .associateWith {op ->
                op.plan.label
                    .annots
                    .filter { it.isLiteral && it.toString().startsWith("preference") }
                    .let {
                        if (it.isEmpty())
                            Double.MAX_VALUE
                        else
                            (((it.first().capply(op.unifier) as Literal).getTerm(0)) as NumberTerm).solve()
                    }
            }

    fun sortedOptions(options: MutableList<Option>, ascending: Boolean) : List<Option> =
        optionPrefs(options)
            .toList()
            .sortedBy { (_,v) -> if (ascending) v else -v }
            .unzip()
            .first


    override fun selectOption(options: MutableList<Option>): Option? =
        /*sortedOptions(options,true)
            .first()*/
        optionPrefs(options)
            .minByOrNull { it.value }
            ?. key
            ?: super.selectOption(options)

        /*options
            .associateWith {
                it.plan.label
                    .annots
                    .filter { it.isLiteral && it.toString().startsWith("preference") }
            }
            .filterValues { it.isNotEmpty() }
            .minByOrNull { (it.value.first().capply(it.key.unifier) as Literal).getTerm(0) }
            ?. key
            ?: super.selectOption(options)*/
}