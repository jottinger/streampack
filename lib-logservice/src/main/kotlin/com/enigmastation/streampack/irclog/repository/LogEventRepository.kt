/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.repository

import com.enigmastation.streampack.irclog.model.LogEvent
import com.enigmastation.streampack.irclog.model.LogEventType
import com.enigmastation.streampack.whiteboard.model.MessageSource
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LogEventRepository : JpaRepository<LogEvent, UUID> {
    @Query(
        value =
            "select e from LogEvent e where lower(e.channel) = lower(:channel) and e.eventType in :types and e.timestamp > :cutoff order by e.timestamp desc"
    )
    fun findByChannel(
        @Param("channel") channel: String,
        @Param("cutoff") cutoff: OffsetDateTime,
        @Param("types") types: Set<LogEventType>,
        page: Pageable
    ): Page<LogEvent>

    @Query(
        value =
            "select e from LogEvent e where lower(e.nick) = lower(:nick) and e.eventType in :types and e.timestamp > :cutoff order by e.timestamp desc"
    )
    fun findByNick(
        @Param("nick") nick: String,
        @Param("cutoff") cutoff: OffsetDateTime,
        @Param("types") types: Set<LogEventType>,
        page: Pageable
    ): Page<LogEvent>

    @Query(
        value =
            """
        select e from LogEvent e 
        where e.source=:source 
        and lower(e.server)=lower(:server) 
        and lower(e.channel)=lower(:channel)
        and FUNCTION('DATE',e.timestamp) = :date
        order by e.timestamp asc
        """
    )
    fun findLogsForChannel(
        @Param("source") source: MessageSource,
        @Param("server") server: String,
        @Param("channel") channel: String,
        @Param("date") date: LocalDate,
        page: Pageable
    ): Page<LogEvent>
}
