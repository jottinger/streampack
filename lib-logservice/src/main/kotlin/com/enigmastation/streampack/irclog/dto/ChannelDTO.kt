/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.dto

import com.enigmastation.streampack.whiteboard.model.MessageSource

data class ChannelDTO(
    val source: MessageSource? = null,
    val name: String? = null,
    val server: String? = null
)
