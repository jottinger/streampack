/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc.service.commands

import com.enigmastation.streampack.irc.service.IrcRouterService
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.RouterMessage

class SetCommand(service: IrcRouterService, val channelService: ChannelService) :
    IrcCommandOperation(service) {
    override fun getCommand(): String {
        return "~set "
    }

    /** Format is ~set parameter #channel value */
    override fun processCommand(message: RouterMessage, content: List<String>): RouterMessage? {
        if (content.size > 2 && isChannel(content[1])) {
            try {
                val value =
                    when (content[2]) {
                        "on",
                        "true",
                        "yes" -> true
                        "off",
                        "false",
                        "no" -> false
                        else -> throw IllegalArgumentException()
                    }
                when (content[0]) {
                    "mute" -> service.mute(content[1], value)
                    "autojoin",
                    "auto_join" -> service.autojoin(content[1], value)
                    "visible" -> service.visible(content[1], value)
                    "logged" -> service.logged(content[1], value)
                    else -> throw IllegalArgumentException()
                }
            } catch (_: Throwable) {
                /* we really don't care what happens if the command is wrong. */
            }
            acknowledge()
        }
        return null
    }
}
