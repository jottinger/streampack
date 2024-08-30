/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import org.springframework.stereotype.Service

@Service
class HelloService : RouterService() {
    var lastMessage: RouterMessage? = null

    fun fireEvent(value: String = "hello") {
        val message = routerMessage {
            content = value
            operation = name
        }
        dispatch(message)
    }

    override fun handleMessage(message: RouterMessage) {
        logger.debug("Received message: {}", message)
        lastMessage = message
    }
}
