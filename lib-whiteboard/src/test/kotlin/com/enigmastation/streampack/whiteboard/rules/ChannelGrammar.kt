/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.rules

import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.rule.Rule

open class ChannelGrammar : ExtendedGrammar<MutableList<String>>() {
    override fun start(): Rule<MutableList<String>> =
        sequence(
            push { mutableListOf<String>() },
            zeroOrMore(whitespace()),
            oneOrMore(
                sequence(
                    choice(
                        sequence(
                            channel(),
                            push {
                                val list = pop(it)
                                list.add(it.previousMatch!!.toString())
                                list
                            }
                        ),
                        word(),
                        oneOrMore(whitespace()),
                    )
                )
            )
        )
}
