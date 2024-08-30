/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.repository

import com.enigmastation.streampack.rss.entity.RSSFeed
import java.net.URL
import java.util.Optional
import java.util.UUID
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "streampack.rss", name = ["enabled"], matchIfMissing = false)
interface RSSFeedRepository : JpaRepository<RSSFeed, UUID> {
    // this method should be abstracted by the RSSFeedService.
    fun findByFeedUrlOrUrl(url: URL, otherUrl: URL): Optional<RSSFeed>
}
