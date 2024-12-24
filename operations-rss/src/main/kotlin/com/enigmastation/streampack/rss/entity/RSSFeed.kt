/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.entity

import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.rss.dto.RSSFeedDTO
import jakarta.persistence.*
import java.net.URL
import java.time.OffsetDateTime
import java.util.*

@Entity
class RSSFeed(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    var title: String? = null,
    var url: URL? = null,
    var feedUrl: URL? = null,
    @ManyToMany var contexts: Set<Channel> = setOf(),
    var createDate: OffsetDateTime? = null,
    var updateDate: OffsetDateTime? = null,
) {
    @PrePersist
    fun persistEntity() {
        createDate = OffsetDateTime.now()
        updateEntity()
    }

    @PreUpdate
    fun updateEntity() {
        updateDate = OffsetDateTime.now()
    }

    fun toDTO(): RSSFeedDTO {
        return RSSFeedDTO(id, title, url, feedUrl, createDate, updateDate)
    }
}
