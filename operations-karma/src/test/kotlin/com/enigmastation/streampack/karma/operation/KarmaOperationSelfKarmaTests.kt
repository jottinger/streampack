/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.karma.repository.KarmaEntryRepository
import com.enigmastation.streampack.karma.service.KarmaConfiguration
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KarmaOperationSelfKarmaTests {
    @Autowired lateinit var karmaEntryRepository: KarmaEntryRepository
    lateinit var setKarmaOperation: SetKarmaOperation

    @Autowired lateinit var karmaEntryService: KarmaEntryService

    @BeforeTest
    fun setupKarmaOperation() {
        val config = KarmaConfiguration()
        setKarmaOperation = SetKarmaOperation()
        setKarmaOperation.karmaEntryService = karmaEntryService
        setKarmaOperation.karmaConfiguration = config
        setKarmaOperation.karmaConfiguration.selfKarmaAllowed = false

        karmaEntryRepository.deleteAll()
    }

    @Test
    fun `26-test that self-karma is not allowed`() {
        val result =
            setKarmaOperation.handleMessage(
                routerMessage {
                    source = "foo"
                    content = "~foo ++ I'm the best"
                }
            )
        assertEquals("You can't increment your own karma! Your karma is now -1.", result?.content)
    }

    @Test
    fun `26-test that self-karma is allowed`() {
        // if comments are disabled, "~foo -- well, this is odd" -- should return false from
        // canHandle().
        // we're enabling them, so it should return "true."
        setKarmaOperation.karmaConfiguration.selfKarmaAllowed = true
        val result =
            setKarmaOperation.handleMessage(
                routerMessage {
                    source = "foo"
                    content = "~foo ++ I'm the best"
                }
            )
        assertEquals("foo now has karma of 1.", result?.content)
    }

    @Test
    fun `26-test that karma comments are recorded`() {
        setKarmaOperation.karmaConfiguration.selfKarmaAllowed = false
        setKarmaOperation.karmaConfiguration.commentsEnabled = true
        setKarmaOperation.handleMessage(
            routerMessage {
                source = "foo"
                content = "~foo ++ I'm the best"
            }
        )
        setKarmaOperation.handleMessage(
            routerMessage {
                source = "bar"
                content = "~foo ++ you're the worst"
            }
        )
        // okay, we should be able to get the most recent comments for "foo", in LIFO order.
        val summary = karmaEntryService.getKarma("foo")
        assertEquals(2, summary.comments.size)
        assertEquals("you're the worst", summary.comments.first())
    }
}
