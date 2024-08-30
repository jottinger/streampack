/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import org.springframework.stereotype.Service

@Service
class OtherService : RouterService() {
    var lastMessage: RouterMessage? = null

    override fun handleMessage(message: RouterMessage) {
        logger.info("Received command: {}", message)
        lastMessage = message
    }
}
