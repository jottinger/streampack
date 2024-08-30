/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc.service

import com.enigmastation.streampack.irc.service.commands.JoinCommand
import com.enigmastation.streampack.irc.service.commands.LeaveCommand
import com.enigmastation.streampack.irc.service.commands.SetCommand
import com.enigmastation.streampack.irclog.model.LogEventType
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.security.service.UserService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import com.enigmastation.streampack.whiteboard.service.Router
import java.util.Objects
import kotlin.jvm.optionals.getOrNull
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.event.channel.ChannelCtcpEvent
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent
import org.kitteh.irc.client.library.event.channel.ChannelTopicEvent
import org.kitteh.irc.client.library.event.channel.RequestedChannelJoinCompleteEvent
import org.kitteh.irc.client.library.event.client.ClientNegotiationCompleteEvent
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent
import org.kitteh.irc.client.library.feature.auth.SaslPlain
import org.kitteh.irc.client.library.util.StsUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@ConfigurationProperties("streampack.irc")
@Service
@ConditionalOnProperty(prefix = "streampack.irc", name = ["enabled"], matchIfMissing = false)
class IrcRouterService() : RouterService(), InitializingBean {
    var nick: String = "streampack"
    var realname: String = "streampack"
    var host: String = "irc.libera.chat"
    var port: Int = 6667
    var sasl: Boolean = true
    var saslAccount: String? = null
    var saslPassword: String? = null
    var channels: String = "#kitteh"
    lateinit var internalRouter: Router

    @Autowired lateinit var channelService: ChannelService

    @Autowired lateinit var userService: UserService

    val inputLogger = LoggerFactory.getLogger("com.enigmastation.streampack.irc.service.input")
    val outputLogger = LoggerFactory.getLogger("com.enigmastation.streampack.irc.service.output")
    val exceptionLogger =
        LoggerFactory.getLogger("com.enigmastation.streampack.irc.service.exception")
    lateinit var client: Client

    fun connect() {
        client.connect()
    }

    open fun join(channel: String) {
        client.addChannel(channel)
    }

    open fun leave(channel: String) {
        client.getChannel(channel).ifPresent { it.part() }
    }

    override fun afterPropertiesSet() {
        client =
            Client.builder()
                .nick(nick)
                .server()
                .host(host)
                // .port(port, Client.Builder.Server.SecurityType.SECURE)
                .then()
                .management()
                .stsStorageManager(StsUtil.getDefaultStorageManager())
                .then()
                .realName(realname)
                .user(realname)
                .listeners()
                .input({ inputLogger.debug("{}", it) })
                .output({ outputLogger.debug("{}", it) })
                .exception({ exceptionLogger.error("{}", it.message, it) })
                .then()
                .build()

        if (sasl) {
            val account: String = Objects.requireNonNull(saslAccount)!!
            val password = Objects.requireNonNull(saslPassword)!!
            client.authManager.addProtocol(SaslPlain(client, account, password))
        }
        logger.info("setting event listener")
        client.eventManager.registerEventListener(this)
        internalRouter =
            Router(
                listOf(),
                listOf(JoinCommand(this), LeaveCommand(this), SetCommand(this, channelService)),
                listOf()
            )
    }

    override fun canHandle(message: RouterMessage): Boolean {
        logger.debug("canHandle message: {}", message)
        return message.messageSource == MessageSource.IRC
    }

    override fun handleMessage(message: RouterMessage) {
        if (message.content.isNotBlank()) {
            val destination: String = message.context ?: message.target ?: return
            // okay, we have a destination. if it's a channel, we need to see if it's muted.
            var muted = false
            if (destination.startsWith("#")) {
                channelService.getChannel(MessageSource.IRC, host, destination).ifPresent { channel
                    ->
                    muted = channel.muted == true
                }
            }
            if (!muted) {
                channelService.logEvent(
                    nick = nick,
                    channel = destination,
                    message = message.content,
                    source = MessageSource.IRC,
                    server = this.host,
                    type = LogEventType.MESSAGE,
                )

                client.sendMultiLineMessage(destination, message.content)
            }
        }
    }

    override fun handleCommand(message: RouterMessage) {
        when (message.content) {
            ".init." -> connect()
            else -> {}
        }
    }

    @Handler
    fun onChannelAction(event: ChannelCtcpEvent) {
        logger.debug("channel notice: {}", event)
        if (event.message.startsWith("ACTION")) {
            channelService.logEvent(
                nick = nick,
                channel = event.channel.name,
                message = event.message.removePrefix("ACTION "),
                source = MessageSource.IRC,
                server = this.host,
                type = LogEventType.ACTION,
            )
        }
    }

    @Handler
    fun onConnected(event: ClientNegotiationCompleteEvent) {
        logger.info("Connected!")
        /*
         * okay, we need to build a list of channels to join.
         * The channels attribute is the basis; we look for these channels in the database, and
         * if they're not there, add them, and set the autojoin to true.
         *
         * Then we just iterate through all the channels, and if they're autojoined, join them.
         */
        channels.split(",").forEach { channel ->
            channelService.update(MessageSource.IRC, host, channel, null, true)
        }
        channelService
            .findAllAutojoinedChannels()
            .mapNotNull { channel -> channel.name }
            .forEach { channel ->
                var channelRef = channelService.getChannel(MessageSource.IRC, host, channel)
                if (channelRef.isEmpty) {
                    // this is specifically marked as an autojoin channel. Make it so.
                    channelService.update(MessageSource.IRC, host, channel, null, true)
                    channelRef = channelService.getChannel(MessageSource.IRC, host, channel)
                }
                if (channelRef.get().autoJoin == true) {
                    join(channel)
                }
            }
    }

    @Handler
    fun onChannelMessage(event: ChannelMessageEvent) {
        // not a private message, let's log it.
        channelService.logEvent(
            nick = event.actor.nick,
            channel = event.channel.name,
            message = event.message,
            source = MessageSource.IRC,
            server = this.host,
            type = LogEventType.MESSAGE,
        )
        // don't dispatch anything if *we* posted it!
        if (event.actor.nick != nick) {
            dispatch(
                routerMessage {
                    content = event.message
                    messageSource = MessageSource.IRC
                    scope = MessageScope.PUBLIC
                    server = host
                    source = event.actor.nick
                    context = event.channel.name
                    cloak = event.actor.host
                }
            )
        }
    }

    @Handler
    fun onChannelJoinedEvent(event: RequestedChannelJoinCompleteEvent) {
        channelService.update(
            MessageSource.IRC,
            host,
            event.channel.name,
            event.channel.topic.value.getOrNull()
        )
        channelService.logEvent(
            nick,
            event.channel.name,
            "",
            MessageSource.IRC,
            this.host,
            LogEventType.JOIN
        )
    }

    @Handler
    fun onChannelTopicChangeEvent(event: ChannelTopicEvent) {
        channelService.update(
            MessageSource.IRC,
            host,
            event.channel.name,
            event.channel.topic.value.getOrNull()
        )
        channelService.logEvent(
            nick = nick,
            channel = event.channel.name,
            message = event.channel.topic.value.getOrNull(),
            source = MessageSource.IRC,
            server = this.host,
            type = LogEventType.TOPIC,
        )
    }

    @Handler
    fun onUserPart(event: ChannelPartEvent) {
        channelService.logEvent(
            nick = event.actor.nick,
            channel = event.channel.name,
            message = "parted",
            source = MessageSource.IRC,
            server = this.host,
            type = LogEventType.PART
        )
    }

    // this is a mechanism that will look for a user auth and apply it to ONE OPERATION
    fun withAuthentication(cloak: String, requiredRole: String, function: () -> Unit) {
        val user = userService.findByCloak(cloak)
        if (user.isPresent) {
            val u = user.get()
            if (u.hasRole(requiredRole)) {
                // eventually we'll have something to call here
                logger.info("role requirement of $requiredRole passed for user ${u.username}")
                function()
            } else {
                logger.info(
                    "role requirement of $requiredRole failed for user ${u.username}: has ${u.roles}"
                )
            }
        } else {
            logger.info("No user found for cloak {}", cloak)
        }
    }

    @Handler
    fun onPrivateMessage(event: PrivateMessageEvent) {
        logger.debug("private message received: {}", event)

        channelService.logEvent(
            event.actor.nick,
            "",
            event.message,
            MessageSource.IRC,
            host,
            LogEventType.PRIVMSG
        )
        // don't dispatch anything if *we* posted it! even if it's a privmsg!
        if (event.actor.nick != nick) {
            val message = routerMessage {
                content = event.message
                messageSource = MessageSource.IRC
                scope = MessageScope.PRIVATE
                source = event.actor.nick
                cloak = event.actor.host
            }
            // we need to look for "join" and "leave" commands. hmm.
            // event.actor.host is the hostname: it's a cloak.
            withAuthentication(event.actor.host, "ADMIN") { internalRouter.dispatch(message) }

            dispatch(message)
        }
    }

    fun mute(channelName: String, muteStatus: Boolean) {
        logger.debug("muting {} with {}", channelName, muteStatus)
        channelService.mute(MessageSource.IRC, host, channelName, muteStatus)
    }

    fun autojoin(channelName: String, autoJoinStatus: Boolean) {
        logger.debug("setting autojoin in {} with {}", channelName, autoJoinStatus)
        channelService.autojoin(MessageSource.IRC, host, channelName, autoJoinStatus)
    }

    fun visible(channelName: String, visible: Boolean) {
        logger.debug("setting visible in {} with {}", channelName, visible)
        channelService.visible(MessageSource.IRC, host, channelName, visible)
    }

    fun logged(channelName: String, logged: Boolean) {
        logger.debug("setting logged in {} with {}", channelName, logged)
        channelService.logged(MessageSource.IRC, host, channelName, logged)
    }
}
