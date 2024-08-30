/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(prefix = "streampack.router.hello.transformer")
class HelloRouterOperation(name: String? = null, priority: Int = 10) :
    RouterOperation(name, priority) {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.indexOf("hello") != -1
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        logger.debug("received message {}", message)
        return message.respondWith(message.content.replace("hello", "hi there"))
    }

    override fun description(): String {
        return "says 'hi there!' if greeted with ~hello"
    }

    override fun longDescription(): String {
        return "says 'hi there!' if greeted with ~hello. Very simple command."
    }
}
