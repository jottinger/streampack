/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.operation

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import com.github.mpe85.grampa.rule.Rule

open class CalcOperationGrammar : ExtendedGrammar<String>() {
    override fun start(): Rule<String> =
        sequence(
            push(""),
            char('~'),
            string("calc"),
            wsp(),
            oneOrMore(sequence(word(), push { pop(it) + it.previousMatch }, optional(wsp())))
        )

    companion object {
        fun parser() = Parser(CalcOperationGrammar::class.createGrammar())
    }
}
