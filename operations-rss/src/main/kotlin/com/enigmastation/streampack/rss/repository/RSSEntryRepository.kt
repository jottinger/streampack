/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.repository

import com.enigmastation.streampack.rss.entity.RSSEntry
import com.enigmastation.streampack.rss.entity.RSSFeed
import java.net.URL
import java.util.Optional
import java.util.UUID
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "streampack.rss", name = ["enabled"], matchIfMissing = false)
interface RSSEntryRepository : JpaRepository<RSSEntry, UUID> {
    fun findByUrl(url: URL): Optional<RSSEntry>

    fun findByFeedOrderByPublishedDesc(feed: RSSFeed): List<RSSEntry>
}
