/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.rule.Rule

open class UrlTitleGrammar : ExtendedGrammar<String>() {
    override fun start(): Rule<String> =
        sequence(
            zeroOrMore(whitespace()),
            zeroOrMore(
                sequence(
                    choice(sequence(url(), push { it.previousMatch.toString() }), word()),
                    zeroOrMore(whitespace())
                )
            )
        )
}
