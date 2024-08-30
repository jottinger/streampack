/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.service

import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.irclog.model.LogEvent
import com.enigmastation.streampack.irclog.model.LogEventType
import com.enigmastation.streampack.irclog.repository.ChannelRepository
import com.enigmastation.streampack.irclog.repository.LogEventRepository
import com.enigmastation.streampack.whiteboard.model.MessageSource
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KFunction4
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    val repository: ChannelRepository,
    val logEventRepository: LogEventRepository
) {
    val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun update(
        source: MessageSource,
        server: String,
        name: String,
        topic: String? = null,
        autoJoin: Boolean = false
    ): Channel {
        var channel =
            repository.findBySourceAndServerIgnoreCaseAndNameIgnoreCase(source, server, name)
        // we don't have one? We will now.
        if (channel.isEmpty) {
            channel =
                Optional.of(
                    save(
                        Channel(
                            source = source,
                            server = server,
                            name = name,
                            logged = true,
                            visible = true,
                            autoJoin = autoJoin,
                            muted = false
                        )
                    )
                )
            logger.debug("Saved new channel {}, topic {}", channel.get().name, channel.get().topic)
        }
        if (topic != null) {
            channel.get().topic = topic
        }
        channel.get().lastSeen = OffsetDateTime.now()
        return channel.get()
    }

    @Transactional
    fun save(channel: Channel): Channel {
        logger.trace("Saving IRC channel {}", channel)
        return repository.save(channel)
    }

    @Transactional
    fun getChannel(source: MessageSource, server: String, name: String): Optional<Channel> {
        logger.trace("getting IRC channel {}, {}, {}", source, server, name)
        return repository.findBySourceAndServerIgnoreCaseAndNameIgnoreCase(source, server, name)
    }

    @Transactional
    fun findAllAutojoinedChannels(): List<Channel> {
        logger.trace("getting autojoined IRC channels")
        return repository.findByAutoJoin(true)
    }

    @Transactional
    fun logEvent(
        nick: String?,
        channel: String?,
        message: String?,
        source: MessageSource,
        server: String? = null,
        type: LogEventType = LogEventType.MESSAGE
    ) {
        // update activity for channel... if it's on a server
        server?.let {
            if (!channel.isNullOrBlank()) {
                update(source, server, channel)
            }
        }
        logEventRepository.save(
            LogEvent(
                nick = nick,
                channel = channel,
                message = message,
                source = source,
                server = server,
                eventType = type
            )
        )
    }

    fun findByNick(nick: String, count: Int = 100): List<LogEvent> {
        return findByType(nick, logEventRepository::findByNick, count)
    }

    fun findByChannel(channel: String, count: Int = 100): List<LogEvent> {
        return findByType(channel, logEventRepository::findByChannel, count)
    }

    private fun findByType(
        selector: String,
        function: KFunction4<String, OffsetDateTime, Set<LogEventType>, Pageable, Slice<LogEvent>>,
        count: Int
    ): List<LogEvent> {
        val backFillTo = OffsetDateTime.now().minusHours(1)
        val entries = mutableListOf<LogEvent>()
        var window =
            function(
                selector,
                backFillTo,
                setOf(LogEventType.ACTION, LogEventType.MESSAGE),
                PageRequest.of(0, 50)
            )
        do {
            entries += window.content
            window =
                function(
                    selector,
                    backFillTo,
                    setOf(LogEventType.ACTION, LogEventType.MESSAGE),
                    window.nextPageable()
                )
        } while (entries.size < count && (!window.isEmpty) && window.hasNext())

        return entries
    }

    @Transactional
    fun mute(source: MessageSource, server: String, channelName: String, muteStatus: Boolean) {
        logger.debug("muting {} with {}", channelName, muteStatus)
        getChannel(source, server, channelName).ifPresent { it.muted = muteStatus }
    }

    @Transactional
    fun autojoin(
        source: MessageSource,
        server: String,
        channelName: String,
        autoJoinStatus: Boolean
    ) {
        logger.debug("setting autojoin in {} with {}", channelName, autoJoinStatus)
        getChannel(source, server, channelName).ifPresent { it.autoJoin = autoJoinStatus }
    }

    @Transactional
    fun visible(source: MessageSource, server: String, channelName: String, visible: Boolean) {
        logger.debug("setting visible in {} with {}", channelName, visible)
        getChannel(source, server, channelName).ifPresent { it.visible = visible }
    }

    @Transactional
    fun logged(source: MessageSource, server: String, channelName: String, logged: Boolean) {
        logger.debug("setting logged in {} with {}", channelName, logged)
        getChannel(source, server, channelName).ifPresent { it.logged = logged }
    }

    fun findById(id: UUID) = repository.findById(id)

    fun findAll(): List<Channel> {
        return repository.findAll()
    }

    fun findLogs(
        source: MessageSource,
        server: String,
        name: String,
        date: LocalDate,
        pageNumber: Int,
        pageSize: Int
    ): Page<LogEvent> {
        return logEventRepository.findLogsForChannel(
            source,
            server,
            name,
            date,
            PageRequest.of(pageNumber, pageSize)
        )
    }
}
