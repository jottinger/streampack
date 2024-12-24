package com.enigmastation.streampack.rss.handler

import com.enigmastation.streampack.rss.entity.RSSEntry
import com.enigmastation.streampack.rss.entity.RSSFeed
import com.enigmastation.streampack.rss.repository.RSSEntryRepository
import com.enigmastation.streampack.rss.service.RSSFeedService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RSSHandler(
    val rssFeedService: RSSFeedService,
    val rssEntryRepository: RSSEntryRepository
) {
    @GetMapping("/feeds", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeeds(): ResponseEntity<List<RSSFeed>> {
        return ResponseEntity.ok(rssFeedService.allFeeds())
    }

    /**
     * This returns the feed, except without the attached contexts. We don't betray contexts.
     */
    @GetMapping("/feeds/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeedById(@PathVariable("id") id: String): ResponseEntity<RSSFeed> {
        val feed = rssFeedService.findByKey(id)
        return feed
            .map {
                with(it) {
                    val feedCopy = RSSFeed(
                        it.id,
                        it.title,
                        it.url,
                        it.feedUrl,
                        setOf(),
                        it.createDate,
                        it.updateDate
                    )
                    ResponseEntity.ok(feedCopy)
                }
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/feeds/{id}/entries", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeedEntries(
        @PathVariable("id") id: String,
        @RequestParam("page") page: Int = 0,
        @RequestParam("pageSize") pageSize: Int = 20
    ): ResponseEntity<Page<RSSEntry>> {
        val feed = rssFeedService.findByKey(id)
        return feed
            .map {
                val pageRequest = PageRequest.of(page, pageSize);
                val entries = rssEntryRepository.findByFeedOrderByPublishedDesc(it, pageRequest)

                ResponseEntity.ok(entries)
            }
            .orElse(ResponseEntity.notFound().build())
    }

}
