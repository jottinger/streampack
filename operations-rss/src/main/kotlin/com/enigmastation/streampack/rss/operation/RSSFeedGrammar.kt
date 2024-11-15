/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.operation

import com.enigmastation.streampack.rss.model.RSSAction
import com.enigmastation.streampack.rss.model.RSSActionOperation
import com.enigmastation.streampack.rules.ExtendedGrammar
import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import com.github.mpe85.grampa.rule.Rule

/**
 * This grammar represents the following parse trees:
 *
 * ~rss add [url] ~rss delete [url] ~rss list
 */
open class RSSFeedGrammar : ExtendedGrammar<RSSAction>() {
    override fun start(): Rule<RSSAction> =
        sequence(
            push { RSSAction() },
            char('~'),
            optionalWsp(),
            string("rss"),
            wsp(),
            sequence(
                choice(string("add"), string("del"), string("delete"), string("info")),
                push {
                    val operation = when (it.previousMatch!!.toString()) {
                        "del" -> "delete"
                        else -> it.previousMatch.toString()
                    }
                    pop(it)
                        .setAction(
                            RSSActionOperation.valueOf(operation.uppercase())
                        )
                },
                wsp(),
                sequence(url(), push { pop(it).setUrl(it.previousMatch!!.toString()) })
            )
        )

    companion object {
        fun parser() = Parser(RSSFeedGrammar::class.createGrammar())
    }
}
