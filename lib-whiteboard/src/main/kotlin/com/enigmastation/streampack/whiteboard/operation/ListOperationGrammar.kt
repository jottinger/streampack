/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import com.github.mpe85.grampa.rule.Rule

open class ListOperationGrammar : ExtendedGrammar<Unit>() {
    override fun start(): Rule<Unit> =
        sequence(char('~'), optionalWsp(), string("list"), wsp(), string("operations"))

    companion object {
        fun parser() = Parser(ListOperationGrammar::class.createGrammar())
    }
}
