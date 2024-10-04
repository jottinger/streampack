/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.service

import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.rss.entity.RSSEntry
import com.enigmastation.streampack.rss.entity.RSSFeed
import com.enigmastation.streampack.rss.repository.RSSEntryRepository
import com.enigmastation.streampack.rss.repository.RSSFeedRepository
import com.enigmastation.streampack.summary.service.SummarizeService
import com.enigmastation.streampack.web.service.JsoupService
import com.enigmastation.streampack.web.service.OkHttpService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import jakarta.transaction.Transactional
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Optional
import org.hibernate.Hibernate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RSSFeedService(
    val rssFeedRepository: RSSFeedRepository,
    val rssEntryRepository: RSSEntryRepository,
    val channelService: ChannelService,
    val jsoupService: JsoupService,
    val okHttpService: OkHttpService,
    var summarizeService: SummarizeService
) {
    companion object {
        private val acceptableRSSTypes =
            listOf<String>(
                "application/rss+xml",
                "application/atom+xml",
                "application/rdf+xml",
                "application/rss",
                "application/atom",
                "application/rdf",
                "text/rss+xml",
                "text/atom+xml",
                "text/rdf+xml",
                "text/rss",
                "text/atom",
                "text/rdf"
            )

        fun isPartialUrl(url: String): Boolean {
            return url.startsWith("/") && !url.contains("://")
        }

        fun combinePaths(first: String, second: String): String {
            return when {
                first.endsWith("/") && second.startsWith("/") -> first + second.substring(1)
                !first.endsWith("/") && !second.startsWith("/") -> "$first/$second"
                else -> first + second
            }
        }
    }

    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun allFeeds(): List<RSSFeed> {
        val all = rssFeedRepository.findAll()
        all.forEach { Hibernate.initialize(it.contexts) }
        return all
    }

    fun findFeedFromSite(url: String): String {
        // first, let's see if we have an actual feed. If we do, we're good: we have the right URL
        return try {
            readFeed(url)
            url
        } catch (e: Throwable) {
            val doc = jsoupService.get(url)
            val elements = doc.selectXpath("//link[@rel='alternate']")
            if (elements.isNotEmpty()) {
                elements
                    .mapNotNull { element ->
                        try {
                            element.attribute("type").value to element.attribute("href").value
                        } catch (_: Exception) {
                            null
                        }
                    }
                    // get rid of the wrong types
                    .filter { entry -> acceptableRSSTypes.contains(entry.first) }
                    // we have the right type, look at the url
                    .map {
                        var value = it.second
                        when (isPartialUrl(value)) {
                            true -> combinePaths(url, value)
                            else -> value
                        }
                    }
                    .mapNotNull {
                        try {
                            it.toURL()
                            it
                        } catch (_: Throwable) {
                            null
                        }
                    }
                    .firstOrNull() ?: throw IOException("Feed url not found for $url")
            } else {
                throw IOException("Feed url not found for $url")
            }
        }
    }

    /**
     * This method attempts to find a feed by URL. External callers should always use
     * `attemptMunging=true` - this will try to find a feed based on a URL whether it ends with a
     * slash or not. Munging will add a slash or remove it if the "raw url" isn't found, and thus
     * this method will call itself safely (with munging off) if it's attempting to coerce an
     * alternative URL.
     */
    @Transactional
    fun findByUrl(url: String, attemptMunging: Boolean = true): Optional<RSSFeed> {
        val result = findByUrl(url.toURL())
        return if (attemptMunging && result.isEmpty) {
            val hasSlash = url.endsWith("/")
            findByUrl(
                if (hasSlash) {
                    url.removeSuffix("/")
                } else {
                    "${url}/"
                },
                false
            )
        } else {
            result
        }
    }

    @Transactional
    fun findByUrl(url: URL): Optional<RSSFeed> {
        val feed = rssFeedRepository.findByFeedUrlOrUrl(url, url)
        feed.ifPresent { Hibernate.initialize(it.contexts) }
        return feed
    }

    @Transactional
    fun addFeed(
        url: String,
        channelName: String,
        source: MessageSource,
        server: String
    ): List<RSSEntry> {
        var feed: Optional<RSSFeed> = Optional.empty()
        var value =
            try {
                // first, let's see if we have the URL in our database, whether by specified URL
                // (which
                // might be wrong)
                // or the feed URL (in case they gave us the actual feed)
                feed = findByUrl(url)
                if (feed.isEmpty) {
                    // okay, we don't know if this exists yet in our DB.
                    // Let's grab the *actual* url from the site if we can.

                    // if the feed url isn't real, this blows up with an exception so we're done.
                    val feedUrl = findFeedFromSite(url)
                    feed = findByUrl(feedUrl)
                    if (feed.isEmpty) {
                        // cool, we know we have a URL - or else we'd have blown up - and we know
                        // it's not in the database.
                        val rssFeed = saveFeed(url, feedUrl, channelName, source, server)
                        // read the feed and get it populated and return the entries we create to
                        // the
                        // caller.
                        readFeed(rssFeed)
                    } else {
                        emptyList<RSSEntry>()
                    }
                } else {
                    emptyList<RSSEntry>()
                }
            } catch (e: Throwable) {
                logger.info("{}", e.message)
                emptyList<RSSEntry>()
            }
        // make sure the contexts contains the feedContext...
        if (feed.isPresent) {
            val channel = channelService.update(source, server, channelName)
            if (!feed.get().contexts.contains(channel)) {
                feed.get().contexts += channel
            }
        }
        return value
    }

    @Transactional
    fun saveFeed(
        url: String,
        feedUrl: String,
        channelName: String,
        source: MessageSource,
        server: String
    ): RSSFeed {
        // okay, we're saving a feed - we need to get the channel from the channel name and use
        // that.
        val channel = channelService.update(source, server, channelName)
        return rssFeedRepository.save(
            RSSFeed(
                url = URI(url).toURL(),
                feedUrl = URI(feedUrl).toURL(),
                contexts = setOf(channel)
            )
        )
    }

    @Transactional
    fun readFeed(rssFeed: RSSFeed): List<RSSEntry> {
        val feed = readFeed(rssFeed.feedUrl.toString())
        val staleEntries: MutableList<RSSEntry> = mutableListOf(*getEntries(rssFeed).toTypedArray())
        val newEntries = mutableListOf<RSSEntry>()
        feed?.let {
            // first let's update the actual feed itself
            rssFeed.title = feed.title
            rssFeed.url = ((feed.link?.toURL() ?: feed.uri?.toURL())) ?: rssFeed.feedUrl
            // reset the time!
            rssFeed.updateEntity()

            // loop through the entries and capture which ones are new
            newEntries +=
                feed.entries
                    .filter { entry ->
                        val e = rssEntryRepository.findByUrl(entry.link.toURL())
                        if (e.isPresent) {
                            staleEntries.removeIf({ it.url!!.equals(entry.link.toURL()) })
                        }
                        e.isEmpty
                    }
                    .mapNotNull { entry ->
                        try {
                            rssEntryRepository.save(
                                RSSEntry(
                                    feed = rssFeed,
                                    title = entry.title ?: entry.link ?: entry.uri,
                                    url = (entry.link?.toURL()) ?: entry.uri.toURL(),
                                    summary = entry.description?.value ?: "",
                                    summarized = false,
                                    llmSummary = "",
                                    published =
                                        if (entry.publishedDate != null) {
                                            entry.publishedDate
                                                .toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toOffsetDateTime()
                                        } else {
                                            OffsetDateTime.now()
                                        }
                                )
                            )
                        } catch (e: Throwable) {
                            logger.error("Could not save {}: {}", entry, e.message)
                            null
                        }
                    }
                    .toList()
            rssEntryRepository.deleteAll(staleEntries)
        }
        return newEntries
    }

    fun readFeed(url: String): SyndFeed? {
        // logger.info("in getFeed({})", url)
        val feed = okHttpService.getUrl(url)
        val input = SyndFeedInput()
        return try {
            input.build(XmlReader(ByteArrayInputStream(feed.toByteArray(Charsets.UTF_8))))
        } catch (e: FeedException) {
            // println(feed)
            throw IOException("Could not parse response", e)
        }
    }

    @Transactional
    fun findNewEntries(): List<RSSEntry> {
        // get all the feeds....
        return rssFeedRepository
            .findAll()
            // now get all the lists of new entries from those feeds...
            .map { feed -> readFeed(feed) }
            // flatten into a single list...
            .flatten()
            // .. randomize the entries to remove preferences if we have a lot of them
            .shuffled()
    }

    @Transactional
    fun findFeedByUrl(url: String): Optional<RSSFeed> {
        return findByUrl(url)
    }

    @Transactional
    fun getEntries(feed: RSSFeed): List<RSSEntry> {
        return rssEntryRepository.findByFeedOrderByPublishedDesc(feed)
    }

    @Transactional
    fun deleteFeed(url: String, feedContext: String, messageSource: MessageSource, server: String) {
        val feed = findFeedByUrl(url)
        if (feed.isPresent) {
            val f = feed.get()
            val channelToRemove =
                Channel(name = feedContext, source = messageSource, server = server)
            f.contexts = f.contexts.filterNot { it == channelToRemove }.toSet()
            if (f.contexts.isEmpty()) {
                rssEntryRepository.deleteAll(getEntries(f))
                rssFeedRepository.delete(f)
            }
        }
    }

    @Transactional
    fun summarizeSingleEntry() {
        val entry = rssEntryRepository.findRSSEntryBySummarized(false).shuffled().firstOrNull()
        entry?.let { e ->
            logger.info("Attempting to summarize {}", e.url)
            val summary = summarizeService.summarizeURL(e.url!!)
            e.llmSummary = summary.summary
            e.summarized = true
            logger.info("Summarized {} as {}", e.url, e.llmSummary)
            rssEntryRepository.save(e)
        }
    }
}
