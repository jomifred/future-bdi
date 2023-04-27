package example.grid

import jason.asSemantics.Agent
import jason.asSemantics.Option
import jason.asSyntax.Literal
import jason.asSyntax.NumberTerm
import jason.asSyntax.PredicateIndicator

class PreferenceAgent : Agent() {

    override fun selectOption(options: MutableList<Option>): Option {
        val fil = options
            .associate {
                it to it.plan.label.capplyAnnots(it.unifier).toList()
                    .filter { it.isLiteral && it.toString().startsWith("preference") }
            }
            .filter { it.value.isNotEmpty() }
            .min
//            .minBy { l -> (l.value.first() as Literal).functor.length } //getTerm(0) as NumberTerm).solve() }
        for ( (o,p) in fil) {
            val l = p.first() as Literal
            val p : Double = (l.getTerm(0) as NumberTerm).solve()
            println("${o.plan.label.functor} = ${p}")
        }
            //.minBy { ( (it.value.first() as Literal).get(0) as NumberTerm).solve() }
//        println("option with pref $fil")

        return super.selectOption(options)
    }
}