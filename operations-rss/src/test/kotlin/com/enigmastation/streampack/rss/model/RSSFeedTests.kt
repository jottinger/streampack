/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.model

import com.enigmastation.streampack.irclog.model.Channel
import com.enigmastation.streampack.whiteboard.model.MessageSource
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RSSFeedTests {
    @Test
    fun `validate Channel uniqueness in sets`() {
        val channels =
            listOf(
                Channel(UUID.randomUUID(), MessageSource.IRC, "irc.libera.chat", "#java"),
                // duplicate of prior channel with different UUID, testing the kotlin model, not the
                // DDL
                Channel(UUID.randomUUID(), MessageSource.IRC, "irc.libera.chat", "#java"),
                Channel(UUID.randomUUID(), MessageSource.IRC, "irc.libera.chat", "#streampack"),
                Channel(UUID.randomUUID(), MessageSource.IRC, "irc.libera.chat", "#kotlin"),
                Channel(UUID.randomUUID(), MessageSource.DISCORD, "irc.libera.chat", "#java"),
                Channel(UUID.randomUUID(), MessageSource.DISCORD, "irc.libera.chat", "#streampack"),
                Channel(UUID.randomUUID(), MessageSource.DISCORD, "irc.libera.chat", "#kotlin")
            )
        println(channels.joinToString("\n"))
        assertEquals(6, channels.toSet().size)
    }
}
