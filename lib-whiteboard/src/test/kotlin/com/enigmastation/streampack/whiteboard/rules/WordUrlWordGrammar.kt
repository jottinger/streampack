/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.rules

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.rule.Rule

open class WordUrlWordGrammar : ExtendedGrammar<String>() {
    override fun start(): Rule<String> =
        sequence(
            zeroOrMore(whitespace()),
            word(),
            oneOrMore(whitespace()),
            sequence(url(), push { it.previousMatch!!.toString() }),
            oneOrMore(whitespace()),
            word(),
            zeroOrMore(whitespace())
        )
}
