/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.rss.service.RSSFeedService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "streampack.rss", name = ["enabled"], matchIfMissing = false)
class RSSFeedOperation(val rssFeedService: RSSFeedService) : RouterOperation() {
    override fun description(): String {
        return """~rss add [url] will add a feed for this channel, such that new entries will be echoed when they are read.
            |~rss delete [url] will remove the feed for this channel, and ~rss info [url] will give the feed's current status.
        """
            .trimIndent()
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("~rss ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val (command, url) = message.content.compress().removePrefix("~rss ").split(" ")
        if (command.isEmpty() || url.isEmpty()) {
            return null
        }
        return when (command.lowercase()) {
            "add" -> {
                if (message.context.isNullOrBlank()) {
                    return null
                }
                val entries =
                    rssFeedService
                        .addFeed(url, message.context!!, message.messageSource, message.server!!)
                        .take(3)
                if (entries.isNotEmpty()) {
                    message.respondWith(
                        "New entries read from feed at ${entries.first().feed!!.url}: ${
                            entries.map { it.url.toString() }.toList().joinToStringWithAnd()
                        }"
                    )
                } else {
                    message.copy(scope = MessageScope.TERMINAL)
                }
            }
            "delete" -> {
                rssFeedService.deleteFeed(
                    url,
                    message.context!!,
                    message.messageSource,
                    message.server!!
                )
                message.copy(scope = MessageScope.TERMINAL)
            }
            "info" -> {
                val feed = rssFeedService.findByUrl(url)
                when {
                    feed.isEmpty -> null
                    feed.isPresent -> {
                        val f = feed.get()
                        val channelToSearchFor =
                            Channel(
                                name = message.context!!,
                                source = message.messageSource,
                                server = message.server
                            )
                        if (f.contexts.contains(channelToSearchFor)) {
                            val entries = rssFeedService.getEntries(f).take(3)
                            message.respondWith(
                                content =
                                    "Feed: ${f.title}, ${f.url}, with entries ${
                                    entries.map { it.url.toString() }.toList().joinToStringWithAnd()
                                } "
                            )
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
}
