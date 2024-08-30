/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.repository

import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.whiteboard.model.MessageSource
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelRepository : JpaRepository<Channel, UUID> {
    fun findBySourceAndServerIgnoreCaseAndNameIgnoreCase(
        source: MessageSource,
        server: String,
        name: String
    ): Optional<Channel>

    fun findByAutoJoin(autoJoin: Boolean): List<Channel>
}
