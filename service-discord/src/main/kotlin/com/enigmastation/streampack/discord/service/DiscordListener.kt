/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.discord.service

import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordListener(val routerService: DiscordRouterService, val channelService: ChannelService) :
    ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        // we ignore bots! They're the worst! ... or something
        if (event.author.isBot) return
        routerService.onChannelMessage(event)
    }

    override fun onReady(event: ReadyEvent) {
        super.onReady(event)
        // okay, for every guild we're on, loop through every channel we can see, so we can manage
        // them
        event.jda.guilds.forEach { guild ->
            guild.channels.forEach { channel ->
                channelService.getChannel(MessageSource.DISCORD, guild.name, channel.name)
            }
        }
    }
}
