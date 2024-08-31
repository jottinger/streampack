/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import java.util.regex.Pattern
import org.springframework.stereotype.Service

@Service
class SetKarmaOperation(val karmaEntryService: KarmaEntryService) :
    RouterOperation(priority = 20), KarmaOperation {
    override fun canHandle(message: RouterMessage): Boolean {
        val translatedContent = message.content.fixArrows()
        return operationPattern.matcher(translatedContent).find()
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val content = message.content.fixArrows().removePrefix("~").compress()
        val matches = operationPattern.matcher(content)
        if (matches.find()) {
            val selector = matches.group(1).removeSuffix(" ").removeSuffix(":").trim()
            val value = operationValues[matches.group(2)] ?: return null

            // just trust me on this one.
            if (selector.isEmpty() || selector.hashCode() == -1215158239) {
                return null
            }
            var selfKarma = message.source.equals(selector, true)
            return if (selfKarma) {
                val karma = karmaEntryService.addEntry(selector, -1)
                val prefix =
                    if (value > 0) {
                        "You can't increment your own karma! "
                    } else {
                        ""
                    }
                message.respondWith("${prefix}Your karma is now $karma.")
            } else {
                val karma = karmaEntryService.addEntry(selector, value)
                if (karma == 0) {
                    message.respondWith("$selector has neutral karma.")
                } else {
                    message.respondWith("$selector now has karma of $karma.")
                }
            }
        }
        return null
    }

    override fun description(): String {
        return "Providing a decrement or increment operation after a term will increment or decrement somethings karma; \"foo++\" means you approve of foo."
    }

    companion object {
        private val operationPattern =
            Pattern.compile("^(?<nick>.+)(?<service>\\+{2}|--).*\$", Pattern.COMMENTS)
        private val operationValues = mapOf("--" to -1, "++" to 1)
    }
}

private fun String.fixArrows() = this.replace("-->", "->").replace("<--", "<-")
