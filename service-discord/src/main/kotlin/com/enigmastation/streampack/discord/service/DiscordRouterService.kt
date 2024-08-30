/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.discord.service

import com.enigmastation.streampack.irclog.model.LogEventType
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.security.service.UserService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.jvm.optionals.getOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@ConfigurationProperties("streampack.discord")
@Service
class DiscordRouterService : RouterService(), InitializingBean {
    var token: String = ""
    private lateinit var api: JDA
    private lateinit var listenerAdapter: ListenerAdapter

    @Autowired private lateinit var channelService: ChannelService

    @Autowired private lateinit var userService: UserService

    override fun afterPropertiesSet() {
        // not sure what happens here yet
    }

    fun connect() {
        listenerAdapter = DiscordListener(this, channelService)
        api =
            JDABuilder.createDefault(token)
                .addEventListeners(listenerAdapter)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build()
        logger.error("To invite this instance to a Discord service, use:")
        logger.error(
            "https://discord.com/oauth2/authorize?client_id=${api.selfUser.applicationId}&permissions=0&integration_type=0&scope=bot"
        )
    }

    override fun handleCommand(message: RouterMessage) {
        when (message.content) {
            ".init." -> connect()
            else -> {
                // nothing happens
            }
        }
    }

    override fun canHandle(message: RouterMessage): Boolean {
        logger.debug("canHandle message: {}", message)
        return message.messageSource == MessageSource.DISCORD
    }

    override fun handleMessage(message: RouterMessage) {
        // needs source and context
        logger.debug("handle message: {}", message)
        if (message.content.isNotBlank()) {
            // if it's got a context, it's on a channel. If not, we use the cloak, because discord
            // privmsgs use ids, not names, which are not exposed to the API.
            val destination: String = message.context ?: message.cloak ?: return
            // okay, we have a destination. if it's a channel, we need to see if it's muted.
            if (message.server.isNullOrBlank()) {
                // we have a private message.
                // these are never muted.
                sendPrivateMessage(destination, message.content)
            } else {
                // we have a guild message.
                val channelName = destination.removePrefix("#")
                val server = message.server ?: ""
                val channel =
                    channelService
                        .getChannel(MessageSource.DISCORD, server, channelName)
                        .getOrNull()
                if ((channel?.muted) != true) {
                    api.getTextChannelsByName(channelName, true)
                        .filter { channel -> channel.guild.name.equals(server, true) }
                        .firstNotNullOf { channel ->
                            channelService.logEvent(
                                nick = api.selfUser.name,
                                channel = "#$channelName",
                                message = message.content,
                                source = MessageSource.DISCORD,
                                server = server,
                                type = LogEventType.MESSAGE
                            )
                            channel.sendMessage(message.content).complete()
                        }
                }
            }
        }
    }

    private fun sendPrivateMessage(destination: String, messageContent: String) {
        api.openPrivateChannelById(destination).complete()?.sendMessage(messageContent)?.queue()
    }

    fun onChannelMessage(event: MessageReceivedEvent) {
        data class ChannelServerBundle(
            val channelName: String? = null,
            val serverName: String? = null,
            val messageScope: MessageScope = MessageScope.PRIVATE,
            val logEventType: LogEventType = LogEventType.MESSAGE
        )

        // okay, we need to emit logs for this channel and message
        // it MAY not have been in a guild.
        val (channelName, serverName, messageScope, logEventType) =
            when (event.isFromGuild) {
                true ->
                    ChannelServerBundle(
                        "#${event.channel.name}",
                        event.guild.name,
                        MessageScope.PUBLIC,
                        LogEventType.MESSAGE
                    )
                else -> ChannelServerBundle(null, null, MessageScope.PRIVATE, LogEventType.PRIVMSG)
            }

        channelService.logEvent(
            nick = event.author.name,
            // make it look like a channel if it's from a guild
            channel = channelName,
            message = event.message.contentDisplay,
            source = MessageSource.DISCORD,
            // if it's not from a guild, it's a private message.
            server = serverName,
            type = logEventType,
        )
        val message = routerMessage {
            content = event.message.contentDisplay
            messageSource = MessageSource.DISCORD
            scope = messageScope
            source = event.author.name
            server = serverName
            context = channelName
            cloak = event.author.id
        }
        dispatch(message)
    }
}
