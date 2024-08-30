/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc.service.commands

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.irc.service.IrcRouterService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import com.enigmastation.streampack.whiteboard.model.routerMessage

abstract class IrcCommandOperation(val service: IrcRouterService) : RouterOperation() {
    abstract fun getCommand(): String

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val predicate = message.content.compress().removePrefix(getCommand()).split(" ").toList()
        return processCommand(message, predicate)
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith(getCommand())
    }

    @Suppress("SameReturnValue")
    abstract fun processCommand(message: RouterMessage, content: List<String>): RouterMessage?

    fun isChannel(token: String): Boolean {
        return token.startsWith("#")
    }

    fun acknowledge(): RouterMessage {
        // empty router message to terminate processing
        return routerMessage { scope = MessageScope.TERMINAL }
    }
}
