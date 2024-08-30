/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.handler

import com.enigmastation.streampack.irclog.model.LogEvent
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Log", description = "Log operations")
@RestController
class LogHandler(val channelService: ChannelService) {
    @GetMapping("/logs/{source}/{server}/{name}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getLogs(
        @Parameter(required = true) @PathVariable("source", required = true) source: MessageSource,
        @Parameter(
            required = true,
            name = "server",
            description = "The service name associated with the logs being requested",
            example = "irc.libera.chat"
        )
        @PathVariable("server", required = true)
        server: String,
        @Parameter(
            required = true,
            name = "name",
            description = "The channel name for requested logs",
            example = "#java"
        )
        @PathVariable("name", required = true)
        name: String,
        @Parameter(required = false) @RequestParam("date") date: LocalDate = LocalDate.now(),
        @Parameter(required = false) @RequestParam("page") pageNumber: Int = 0,
        @Parameter(required = false) @RequestParam("size") pageSize: Int = 400
    ): Page<LogEvent> {
        val channel = channelService.getChannel(source, server, name)
        return if (channel.isPresent) {
            channelService.findLogs(source, server, name, date, pageNumber, pageSize)
        } else {
            Page.empty<LogEvent>()
        }
    }
}
