/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.repository

import com.enigmastation.streampack.rss.entity.RSSFeed
import java.net.URL
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RSSFeedRepository : JpaRepository<RSSFeed, UUID> {
    // this method should be abstracted by the RSSFeedService.
    fun findByFeedUrlOrUrl(url: URL, otherUrl: URL): Optional<RSSFeed>

    @Query(
        """
        select f 
            from RSSFeed f 
            where 
                lower(f.feedUrl)=lower(:key) or 
                lower(f.url)=lower(:key) or
                lower(cast(f.id as string))=lower(:key)
            """
    )
    fun findByKey(key: String): Optional<RSSFeed>
}
