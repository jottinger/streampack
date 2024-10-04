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
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    indexes =
        [
            Index(columnList = "timestamp"),
            Index(columnList = "nick"),
            Index(columnList = "source,server,channel"),
        ]
)
class LogEvent(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    var nick: String? = null,
    // source, like IRC, DISCORD, etc
    @Enumerated(EnumType.STRING) var source: MessageSource? = null,
    // server, like libera.chat for IRC, or a guild for discord
    var server: String? = null,
    var channel: String? = null,
    @Column(columnDefinition = "text") var message: String? = null,
    @Enumerated(EnumType.STRING) var eventType: LogEventType? = LogEventType.MESSAGE,
    var timestamp: OffsetDateTime? = null
) {
    @PrePersist
    fun updateTimestamp() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now()
        }
    }
}
