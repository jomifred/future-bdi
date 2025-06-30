package jason.agent

import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Literal
import jason.asSyntax.NumberTerm

/** gets the value of a property in the annotations of a plan (an option) */
fun Option.getProp(property: String, default: Double)  =
    this.plan.label
        .annots
        .filter { it.isLiteral && it.toString().startsWith(property) }
        .let {
            if (it.isEmpty())
                default
            else
                (((it.first().capply(this.unifier) as Literal).getTerm(0)) as NumberTerm).solve()
        }
fun Option.getCost() = this.getProp("cost", 1.0)
fun Option.getPreference() = this.getProp("preference", Double.MAX_VALUE)

/** Agent class that select options based on preference */
open class PreferenceAgent : Agent() {

    /*open fun optionProp(options: List<Option>, property: String, default: Double) : Map<Option, Double> =
        options.associateWith {op -> op.getProp(property, default) }

    open fun optionPrefs(options: List<Option>) : Map<Option, Double> =
        optionProp(options, "preference", Double.MAX_VALUE)*/
    //open fun optionCosts(options: MutableList<Option>) : Map<Option, Double> =
    //    optionProp(options, "cost", 1.0)

    open fun optionF(options: List<Option>) : Map<Option, Double> =
        options.associateWith {op -> op.getCost() + op.getPreference() }

    open fun sortedOptions(options: List<Option>, ascending: Boolean = true) : List<Option> =
        optionF(options)
            .toList()
            .sortedBy { (_,v) -> if (ascending) v else -v }
            .unzip()
            .first

    override fun selectOption(options: MutableList<Option>): Option? =
        optionF(options)
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