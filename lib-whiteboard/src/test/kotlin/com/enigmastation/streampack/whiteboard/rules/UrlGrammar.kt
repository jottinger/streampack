/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.rules

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.rule.Rule

open class UrlGrammar : ExtendedGrammar<String>() {
    override fun start(): Rule<String> =
        sequence(zeroOrMore(whitespace()), url(), push { it.previousMatch.toString() })
}
