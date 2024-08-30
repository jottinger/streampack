/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.entity

import com.enigmastation.streampack.irclog.model.Channel
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.net.URL
import java.time.OffsetDateTime
import java.util.UUID

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
}
