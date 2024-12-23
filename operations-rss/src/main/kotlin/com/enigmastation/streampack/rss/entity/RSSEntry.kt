/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.entity

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.net.URL
import java.time.OffsetDateTime
import java.util.UUID

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
}
