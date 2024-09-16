/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.llm.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.isChannelReference
import com.enigmastation.streampack.irclog.model.LogEventType
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.security.service.UserService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service

@Service
class SentimentAnalysisService(
    val ollamaChatModel: OllamaChatModel,
    val channelService: ChannelService,
    val userService: UserService
) : RouterService() {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.cloak != null && message.content.startsWith("~sentiment for ")
    }

    override fun handleMessage(message: RouterMessage) {
        if (!canHandle(message)) {
            return
        }
        val content = message.content.removePrefix("~sentiment for ").compress()
        val reference = content.split(" ")[0]
        var outgoingContext = message.context
        // if there's nothing to gather sentiment *for*, or if the channel for sentiment is not the
        if (reference.isEmpty()) {
            return
        }
        // okay, here're the rules for sentiment:
        // the query must be from an admin.
        // if it's a nick, the sentiment is private.
        // if it's a channel, the sentiment can be public, but it MUST be in the same channel
        // as the query.
        var scope =
            if (reference.isChannelReference()) {
                if (reference.equals(message.context, true)) {
                    message.scope
                } else {
                    // make sure this goes back to the originating PERSON, as a privmsg
                    outgoingContext = null
                    MessageScope.PRIVATE
                }
            } else {
                MessageScope.PRIVATE
            }
        if (
            reference.isChannelReference() &&
                (message.context ?: "").lowercase() == reference.lowercase()
        ) {
            scope = message.scope
        }

        val user = userService.findByCloak("IrcService", message.cloak!!)
        // no user? move on. if they're not an admin? Again, move along.
        // sentiment analysis is easy to abuse.
        if ( user.hasRole("ADMIN")) {
            // okay, so now we have... a person or a channel.
            val messages =
                if (reference.startsWith("#")) {
                    channelService.findByChannel(reference)
                } else {
                    channelService.findByNick(reference)
                }
            if (messages.isEmpty()) {
                return
            }
            val translatedEvents =
                messages
                    .reversed()
                    .mapNotNull {
                        val nick = it.nick
                        val content = it.message
                        when (it.eventType) {
                            LogEventType.ACTION -> "$nick $content"
                            LogEventType.MESSAGE -> "$nick: $content"
                            else -> null
                        }
                    }
                    .joinToString("\n")
            val query =
                """
            Describe the overall sentiment of the following interactions. The conversation is represented by a speaker,
            who can either speak ("foo: bar" means "foo" says "bar") or act 
            ("foo jumps" means "foo is undertaking an act of jumping").
            
            The analysis must be a simple numeric score, where -10 is "very negative sentiment" and 10 is 
            "very positive" - include "negative" or "positive" in the score so users can easily see the meaning.
            
            Also, include the names of the individuals who contribute the most to that score.
            
            Example output might be: "This interaction has a sentiment score of 7, which is "mostly positive," 
            largely driven by the interaction between persons 1 and 2."
            
            ```
            $translatedEvents
            ```
        """
                    .trimIndent()
            val response = ollamaChatModel.call(query)
            logger.info("response: $response")
            response
                .compress()
                .split("\n")
                .filter { it.isNotEmpty() }
                .forEach {
                    dispatch(message.respondWith(it).copy(scope = scope, context = outgoingContext))
                }
        }
    }
}
