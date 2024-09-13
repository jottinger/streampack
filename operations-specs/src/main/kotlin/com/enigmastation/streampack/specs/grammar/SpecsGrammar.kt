/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.grammar

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.enigmastation.streampack.specs.model.SpecsRequest
import com.enigmastation.streampack.specs.model.SpecsRequestType
import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import com.github.mpe85.grampa.rule.Rule

open class SpecsGrammar : ExtendedGrammar<SpecsRequest>() {
    override fun start(): Rule<SpecsRequest> {
        return sequence(
            string("~"),
            optionalWsp(),
            sequence(
                choice(SpecsRequestType.entries.map { ignoreCase(it.toString()) }),
                push {
                    SpecsRequest(
                        type = SpecsRequestType.valueOf(it.previousMatch.toString().uppercase())
                    )
                }
            ),
            optionalWsp(),
            sequence(
                oneOrMore(digit()),
                push {
                    val value = it.previousMatch.toString().toInt()
                    pop(it).copy(identifier = value)
                },
            ),
            optionalWsp()
        )
    }

    companion object {
        val grammar = SpecsGrammar::class.createGrammar()

        fun parser() = Parser(grammar)
    }
}
