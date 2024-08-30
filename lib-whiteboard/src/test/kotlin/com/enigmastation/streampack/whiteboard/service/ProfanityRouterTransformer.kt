/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterTransformer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(prefix = "streampack.router.profanity.transformer")
class ProfanityRouterTransformer(name: String? = null, priority: Int = 10) :
    RouterTransformer(name, priority) {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.indexOf("darn") != -1
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        logger.debug("received message {}", message)
        return message.respondWith(message.content.replace("darn", "d**n"))
    }
}
