/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.karma.service.KarmaConfiguration
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KarmaOperationWithoutCommentTests {
    lateinit var setKarmaOperation: SetKarmaOperation

    @Autowired lateinit var karmaEntryService: KarmaEntryService

    @BeforeTest
    fun setupKarmaOperation() {
        val config = KarmaConfiguration()
        setKarmaOperation = SetKarmaOperation()
        setKarmaOperation.karmaEntryService = karmaEntryService
        setKarmaOperation.karmaConfiguration = config
        setKarmaOperation.karmaConfiguration.commentsEnabled = false
    }

    @Test
    fun `2-validate that operations do not register with comments`() {
        // if comments are disabled, "~foo -- well, this is odd" -- should return false from
        // canHandle()
        assertFalse(
            setKarmaOperation.canHandle(routerMessage { content = "~foo -- well, this is odd" })
        )
    }

    @Test
    fun `2-validate that operations do register with comments`() {
        // if comments are disabled, "~foo -- well, this is odd" -- should return false from
        // canHandle().
        // we're enabling them, so it should return "true."
        setKarmaOperation.karmaConfiguration.commentsEnabled = true
        assertTrue(
            setKarmaOperation.canHandle(routerMessage { content = "~foo -- well, this is odd" })
        )
    }
}
