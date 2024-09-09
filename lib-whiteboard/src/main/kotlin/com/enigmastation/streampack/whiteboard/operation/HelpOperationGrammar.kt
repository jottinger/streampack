/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import com.github.mpe85.grampa.rule.Rule

open class HelpOperationGrammar(val operations: List<RouterOperation>) : ExtendedGrammar<String>() {
    override fun start(): Rule<String> =
        sequence(
            optionalWsp(),
            string("~help"),
            optional(
                sequence(wsp(), sequence(operationName(), push { it.previousMatch!!.toString() }))
            )
        )

    open fun operationName(): Rule<String> {
        val operationNames = operations.map { it.name }

        return choice(operationNames.map { string(it) })
    }

    companion object {
        fun parser(operations: List<RouterOperation>) =
            Parser(HelpOperationGrammar::class.createGrammar(operations))
    }
}
