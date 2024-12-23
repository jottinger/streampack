/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.karma.service.KarmaConfiguration
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import java.util.regex.Pattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SetKarmaOperation() : RouterOperation(priority = 20), KarmaOperation {
    @Autowired lateinit var karmaEntryService: KarmaEntryService
    @Autowired lateinit var karmaConfiguration: KarmaConfiguration

    override fun canHandle(message: RouterMessage): Boolean {
        val translatedContent = message.content.fixArrows()
        val matches = operationPattern.matcher(translatedContent)
        return if (matches.find()) {
            !(karmaConfiguration.commentsEnabled == false && matches.group(3).trim().isNotEmpty())
        } else {
            false
        }
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
            var comment = matches.group(3)
            if (comment.isNotEmpty()) {
                comment = comment.compress()
            }

            // just trust me on this one.
            if (selector.isEmpty() || selector.hashCode() == -1215158239) {
                return null
            }
            var selfKarma = message.source.equals(selector, true)
            return if (selfKarma && !karmaConfiguration.selfKarmaAllowed) {
                val karmaSummary = karmaEntryService.addEntry(selector, -1, comment)
                val karma = karmaSummary.karma.toInt()
                val prefix =
                    if (value > 0) {
                        "You can't increment your own karma! "
                    } else {
                        ""
                    }
                message.respondWith("${prefix}Your karma is now $karma.")
            } else {
                //                val karma = karmaEntryService.addEntry(selector, value, comment)
                val karmaSummary = karmaEntryService.addEntry(selector, value, comment)
                val karma = karmaSummary.karma.toInt()

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
            Pattern.compile("^(?<nick>.+)(?<service>\\+{2}|--)(?<comment>.*)\$", Pattern.COMMENTS)
        private val operationValues = mapOf("--" to -1, "++" to 1)
    }
}

private fun String.fixArrows() = this.replace("-->", "->").replace("<--", "<-")
