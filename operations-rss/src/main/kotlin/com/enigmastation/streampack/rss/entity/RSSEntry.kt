/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.entity

import com.enigmastation.streampack.rss.dto.RSSEntryDTO
import com.enigmastation.streampack.rss.dto.RSSEntryOut
import jakarta.persistence.*
import java.net.URL
import java.time.OffsetDateTime
import java.util.*

@Entity
class RSSEntry(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @ManyToOne(optional = true) var feed: RSSFeed? = null,
    var title: String? = null,
    var url: URL? = null,
    @Column(columnDefinition = "text") var summary: String? = null,
    @Column(columnDefinition = "text") var llmSummary: String? = null,
    @ElementCollection(fetch = FetchType.EAGER) var categories: List<String>? = null,
    var summarized: Boolean? = null,
    var published: OffsetDateTime? = null,
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
        if (summarized == null) {
            summarized = false
        }
        updateDate = OffsetDateTime.now()
    }

    fun toDTO(): RSSEntryOut {
        return RSSEntryDTO(
            id,
            title,
            url,
            summary,
            llmSummary,
            categories,
            published,
            createDate,
            updateDate
        )
    }
}
