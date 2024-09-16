/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.security.entity.RouterUser
import java.time.OffsetDateTime

data class RouterMessage(
    var content: String = "",
    var target: String? = null,
    var source: String? = null,
    // IRC host, or Discord guild
    var server: String? = null,
    // e.g., channel for IRC
    var context: String? = null,
    var operation: String? = null,
    var scope: MessageScope = MessageScope.PRIVATE,
    var messageSource: MessageSource = MessageSource.UNKNOWN,
    var process: Boolean = true,
    // IRC cloak, if any; internal analogue for UserPrincipal
    val cloak: String? = null,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val user: RouterUser? = null
) {
    /** Inverts the message's target and changes the content */
    fun respondWith(content: String): RouterMessage {
        return copy(
            content = content,
            target = source,
            source = target,
            timestamp = OffsetDateTime.now()
        )
    }

    fun compress(): String {
        return content.compress()
    }
}
