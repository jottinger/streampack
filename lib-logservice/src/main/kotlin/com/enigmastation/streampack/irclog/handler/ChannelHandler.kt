/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irclog.handler

import com.enigmastation.streampack.irclog.dto.ChannelDTO
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Channel", description = "Channel operations")
@RestController
class ChannelHandler(val channelService: ChannelService) {
    @Operation(
        summary = "Get a list of all visible channels",
        description = """
            This method gets a list of all visible channels.
        """
    )
    @GetMapping("/channels/{service}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getChannels(
        @Parameter(
            name = "service",
            description = "An optional parameter to limit the type of channel returned",
            required = false,
            examples = [ExampleObject(value = "IRC"), ExampleObject("DISCORD")]
        )
        @PathVariable("service")
        source: MessageSource? = null
    ): List<ChannelDTO> {
        return channelService
            .findAll()
            .filter {
                if (source != null) {
                    it.source == source
                } else {
                    true
                }
            }
            .filter { it.visible == true && it.logged == true }
            .map { ChannelDTO(it.source, it.name, it.server) }
    }
}
