/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.handler

import com.enigmastation.streampack.rss.dto.RSSEntryOut
import com.enigmastation.streampack.rss.dto.RSSFeedDTO
import com.enigmastation.streampack.rss.dto.RSSFeedOut
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
class RSSHandler(val rssFeedService: RSSFeedService, val rssEntryRepository: RSSEntryRepository) {
    @GetMapping("/feeds", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeeds(
        @RequestParam("page") page: Int = 0,
        @RequestParam("pageSize") pageSize: Int = 20
    ): ResponseEntity<Page<RSSFeedOut>> {
        val pageRequest = PageRequest.of(page, pageSize)
        return ResponseEntity.ok(rssFeedService.allFeeds(pageRequest).map { it.toDTO() })
    }

    /** This returns the feed, except without the attached contexts. We don't betray contexts. */
    @GetMapping("/feeds/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeedById(@PathVariable("id") id: String): ResponseEntity<RSSFeedDTO> {
        val feed = rssFeedService.findByKey(id)
        return feed.map { ResponseEntity.ok(it.toDTO()) }.orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/feeds/{id}/entries", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFeedEntries(
        @PathVariable("id") id: String,
        @RequestParam("page") page: Int = 0,
        @RequestParam("pageSize") pageSize: Int = 20
    ): ResponseEntity<Page<RSSEntryOut>> {
        val feed = rssFeedService.findByKey(id)
        return feed
            .map {
                val pageRequest = PageRequest.of(page, pageSize)
                val entries = rssEntryRepository.findByFeedOrderByPublishedDesc(it, pageRequest)

                ResponseEntity.ok(entries.map { rssEntry -> rssEntry.toDTO() })
            }
            .orElse(ResponseEntity.notFound().build())
    }
}
