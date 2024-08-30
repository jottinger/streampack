/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.model

import com.enigmastation.streampack.whiteboard.model.MessageSource
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(indexes = [Index(columnList = "source,server,name", unique = true)])
class Channel(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Enumerated(EnumType.STRING) var source: MessageSource? = null,
    @Column(length = 128) var server: String? = null,
    @Column(length = 64) var name: String? = null,
    var topic: String? = null,
    /** Indicates whether this channel should be joined on startup or not. */
    var autoJoin: Boolean? = null,
    /** Whether this channel's data gets logged at all. Should be "true" for most cases. */
    var logged: Boolean? = null,
    /** Whether *anything* should be sent to this channel, at all, ever */
    var muted: Boolean? = null,
    /**
     * Whether this channel's information is publicly available (i.e., whether the bot is willing to
     * say "yes, I am in this channel" for UI purposes.
     */
    var visible: Boolean? = null,
    var lastSeen: OffsetDateTime? = null,
    var creationTimestamp: OffsetDateTime? = null,
    var updateTimestamp: OffsetDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Channel) return false

        return this.id == other.id ||
            (this.name == other.name && this.server == other.server && this.source == other.source)
    }

    override fun hashCode(): Int {
        return "$name:$server:$source".hashCode()
    }

    override fun toString(): String {
        return "Channel[id=$id,name=$name,server=$server,source=$source]"
    }

    @PrePersist
    fun createTimestamp() {
        if (creationTimestamp == null) {
            creationTimestamp = OffsetDateTime.now()
        }
        updateTimestamp()
    }

    @PreUpdate
    fun updateTimestamp() {
        updateTimestamp = OffsetDateTime.now()
    }
}
