/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class GetKarmaOperation(val karmaEntryService: KarmaEntryService) :
    RouterOperation(), KarmaOperation {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.compress().removePrefix("~").startsWith("karma ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val selector = message.content.compress().removePrefix("~").removePrefix("karma ")
        val karma = karmaEntryService.getKarma(selector)
        return if (karmaEntryService.hasKarma(selector)) {
            val karmaValue = karma.karma.toInt()
            val karmaExpression =
                if (karmaValue == 0) {
                    "neutral karma"
                } else {
                    "karma of $karmaValue"
                }
            if (message.source.equals(selector, true)) {
                message.respondWith("$selector, you have $karmaExpression.")
            } else {
                message.respondWith("$selector has $karmaExpression.")
            }
        } else {
            if (message.source.equals(selector, true)) {
                message.respondWith("$selector, you have no karma data.")
            } else {
                message.respondWith("$selector has no karma data.")
            }
        }
    }

    override fun description(): String {
        return "\"~karma [term]\" will give you the current calculated karma for the term, if any."
    }
}
