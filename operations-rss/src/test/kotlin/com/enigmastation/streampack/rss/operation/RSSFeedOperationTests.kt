/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.operation

import com.enigmastation.streampack.extensions.watchWithTimeout
import com.enigmastation.streampack.rss.model.RSSAction
import com.enigmastation.streampack.rss.model.RSSActionOperation
import com.enigmastation.streampack.rss.repository.RSSEntryRepository
import com.enigmastation.streampack.rss.repository.RSSFeedRepository
import com.enigmastation.streampack.rss.service.RSSFeedService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RSSFeedOperationTests {
    @Autowired lateinit var operation: RSSFeedOperation

    @Autowired lateinit var rssFeedRepository: RSSFeedRepository

    @Autowired lateinit var rssEntryRepository: RSSEntryRepository

    @Autowired lateinit var rssFeedService: RSSFeedService
    val parser = RSSFeedGrammar.parser()

    @ParameterizedTest
    @MethodSource("grammarInputs")
    fun `test grammar`(input: String, matched: Boolean, action: RSSAction?) {
        val result = parser.run(input)
        assertEquals(matched, result.matched)
        result.stackTop?.let {
            assertNotNull(action)
            assertEquals(action.action, it.action)
            assertEquals(action.url, it.url)
        }
    }

    @Test
    fun `context loads`() {
        assertNotNull(operation)
    }

    @BeforeEach
    fun `clear rss data`() {
        rssEntryRepository.deleteAll()
        rssFeedRepository.deleteAll()
    }

    @Test
    fun `rss happy path can handle`() {
        assertTrue(
            operation.canHandle(
                routerMessage {
                    content = "~rss add https://enigmastation.com/"
                    context = "#test"
                    messageSource = MessageSource.IRC
                    server = "irc.libera.chat"
                }
            )
        )
    }

    @Test
    fun `rss requires signal character`() {
        assertFalse(
            operation.canHandle(
                routerMessage {
                    content = "rss add https://enigmastation.com/"
                    context = "#test"
                    messageSource = MessageSource.IRC
                    server = "irc.libera.chat"
                }
            )
        )
    }

    @Test
    fun `rss happy path mutate feed`() {
        val response =
            operation.handleMessage(
                routerMessage {
                    content = "~rss add https://enigmastation.com/"
                    context = "#test"
                    messageSource = MessageSource.IRC
                    server = "irc.libera.chat"
                }
            )
        // this should add an RSS feed at https://enigmastation.com/feed/ to the rss feeds
        watchWithTimeout({ rssFeedRepository.findAll().isNotEmpty() })
        println(response)

        // add another context
        operation.handleMessage(
            routerMessage {
                content = "~rss add https://enigmastation.com/"
                context = "#foobar"
                messageSource = MessageSource.IRC
                server = "irc.libera.chat"
            }
        )
        assertTrue(rssFeedRepository.findAll().isNotEmpty())
        var feed = rssFeedService.findByUrl("https://enigmastation.com/")
        assertTrue(feed.isPresent)
        assertEquals(2, feed.get().contexts.size)
        println("line 100: ${feed.get().url} ${feed.get().feedUrl}")

        var result =
            operation.handleMessage(
                routerMessage {
                    // note missing slash after the url here
                    content = "~rss info https://enigmastation.com"
                    context = "#foobar"
                    messageSource = MessageSource.IRC
                    server = "irc.libera.chat"
                }
            )
        println(result)
        assertNotNull(result)
        assertTrue(result.content.contains("Feed: Enigmastation.com, https://enigmastation.com"))

        operation.handleMessage(
            routerMessage {
                content = "~rss delete https://enigmastation.com/"
                context = "#foobar"
                messageSource = MessageSource.IRC
                server = "irc.libera.chat"
            }
        )
        assertTrue(rssFeedRepository.findAll().isNotEmpty())
        feed = rssFeedService.findByUrl("https://enigmastation.com/")
        assertTrue(feed.isPresent)
        assertEquals(1, feed.get().contexts.size)

        // info should only work in the contexts for which the feed is appropriate
        result =
            operation.handleMessage(
                routerMessage {
                    content = "~rss info https://enigmastation.com"
                    context = "#foobar"
                    messageSource = MessageSource.IRC
                    server = "irc.libera.chat"
                }
            )
        println(result)
        assertNull(result)

        operation.handleMessage(
            routerMessage {
                content = "~rss delete https://enigmastation.com/"
                context = "#test"
                messageSource = MessageSource.IRC
                server = "irc.libera.chat"
            }
        )
        assertTrue(rssFeedRepository.findAll().isEmpty())
    }

    companion object {
        @JvmStatic
        fun grammarInputs(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "~rss add https://enigmastation.com/",
                    true,
                    RSSAction(RSSActionOperation.ADD, "https://enigmastation.com/")
                ),
                Arguments.of("~rss add htts://enigmastation.com/", false, null),
                Arguments.of(
                    "~rss delete https://enigmastation.com/",
                    true,
                    RSSAction(RSSActionOperation.DELETE, "https://enigmastation.com/")
                ),
                Arguments.of(
                    "~rss info http://enigmastation.com",
                    true,
                    RSSAction(RSSActionOperation.INFO, "http://enigmastation.com")
                ),
                Arguments.of(
                    "~ rss  info   https://enigmastation.com    ",
                    true,
                    RSSAction(RSSActionOperation.INFO, "https://enigmastation.com")
                ),
            )
    }
}
