/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.operation

import com.enigmastation.streampack.extensions.toURL
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
                choice(string("add"), string("delete"), string("info")),
                push {
                    pop(it)
                        .setAction(
                            RSSActionOperation.valueOf(it.previousMatch!!.toString().uppercase())
                        )
                },
                wsp(),
                sequence(url(), push { pop(it).setUrl(it.previousMatch!!.toString().toURL()) })
            )
        )

    companion object {
        fun parser() = Parser(RSSFeedGrammar::class.createGrammar())
    }
}
