/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc.service.commands

import com.enigmastation.streampack.irc.service.IrcRouterService
import com.enigmastation.streampack.whiteboard.model.RouterMessage

class LeaveCommand(service: IrcRouterService) : IrcCommandOperation(service) {
    override fun processCommand(message: RouterMessage, content: List<String>): RouterMessage? {
        if (content.isNotEmpty() && isChannel(content.first())) {
            service.leave(content.first())
            acknowledge()
        }
        return null
    }

    override fun getCommand(): String = "~leave "
}
