/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import com.enigmastation.streampack.whiteboard.service.Router
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Ayin is a spring: a thing that produces events and consumes them. This is a service, like IRC,
 * Discord, email, Slack, or HTTP.
 */
abstract class RouterService(name: String? = null) : NamedService(name) {
    val routers = mutableListOf<Router>()

    init {
        timeout = Duration.of(15, ChronoUnit.SECONDS)
    }

    fun addRouter(router: Router) {
        logger.debug("Adding router {}", router)
        routers += router
    }

    fun dispatch(message: RouterMessage) {
        logger.debug("dispatching message {}", message)
        routers.forEach { router -> router.dispatch(message.copy(operation = name)) }
    }

    override fun canHandle(message: RouterMessage): Boolean {
        logger.debug("Service {} canHandle called for {}: returning true", name, message)
        return true
    }

    /**
     * This is the generalized entry point for messages that are dispatched to services. They
     * conform to a NamedService' receive() method, but will *always* return null. There are no
     * future consumers of the service' message.
     */
    final override fun receive(message: RouterMessage): RouterMessage? {
        logger.debug("Service {} received message response {}", name, message)
        this.handleMessage(message)
        return null
    }

    /*
     * This method handles RouterMessages *from the system* - from other RouterServices, or Operations.
     */
    open fun handleMessage(message: RouterMessage) {}

    /**
     * This method handles RouterMessages that are described as "INTERNAL" - init messages, for
     * example.
     */
    open fun handleCommand(message: RouterMessage) {}
}
