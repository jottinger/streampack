/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.service

import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.pluralize
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@ConfigurationProperties(prefix = "streampack.rss")
@ConditionalOnProperty(prefix = "streampack.rss", name = ["enabled"], matchIfMissing = false)
@Service("RSSRouterService")
class RSSRouterService() : RouterService() {
    @Autowired lateinit var feedService: RSSFeedService

    @Autowired lateinit var channelService: ChannelService

    /** This class handles no incoming messages. */
    override fun canHandle(message: RouterMessage): Boolean {
        return false
    }

    @Scheduled(cron = "0 */2 * * * *")
    fun summarizeSingleEntry() {
        feedService.summarizeSingleEntry()
    }

    @Scheduled(cron = "0 0 * * * *")
    fun readFeeds() {
        val contextToEntries = mutableMapOf<UUID?, List<String>>()
        // bleah. What does this need to do?
        // Okay.
        // for every feed, see if there are new entries. This set gets mapped into a list of
        // contexts,
        // so you might have "#metal" to [ url1, url2, url3] and #kitteh to [url2, url3, url4]
        // then, for each context, emit a message with up to three URLs in it.
        feedService.allFeeds().forEach { feed ->
            if (feed.contexts.isNotEmpty()) {
                val entries =
                    feedService.findNewEntries().map { it.url!!.toString() }.toMutableList()

                feed.contexts.forEach { context ->
                    contextToEntries[context.id] =
                        (contextToEntries[context.id] ?: listOf()) + entries

                    val entries = feedService.findNewEntries().take(3)
                }
            } else {
                logger.info("Feed ${feed.url} has blank context: skipping read")
            }
        }
        contextToEntries.forEach { context ->
            val channel =
                channelService.findById(context.key!!).orElseThrow {
                    IllegalArgumentException("channel not found: ${context.key}")
                }
            val entries = context.value.shuffled().take(3).toList()
            if (entries.isNotEmpty()) {
                dispatch(
                    routerMessage {
                        content =
                            "New ${
                                "link".pluralize(entries)
                            }: ${entries.joinToStringWithAnd()}"
                        scope = MessageScope.PUBLIC
                        messageSource = MessageSource.RSS
                        target = channel.name
                        server = channel.server
                        process = false
                    }
                )
            }
        }
    }
}
