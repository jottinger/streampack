/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.repository

import com.enigmastation.streampack.rss.entity.RSSEntry
import com.enigmastation.streampack.rss.entity.RSSFeed
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URL
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RSSEntryRepository : JpaRepository<RSSEntry, UUID> {
    fun findByUrl(url: URL): Optional<RSSEntry>

    fun findByFeedOrderByPublishedDesc(feed: RSSFeed): List<RSSEntry>

    fun findRSSEntryBySummarized(summarized: Boolean): List<RSSEntry>

    fun findByFeedOrderByPublishedDesc(feed: RSSFeed, page:Pageable ): Page<RSSEntry>
}
