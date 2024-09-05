/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.urltitle.repository.IgnoredHostRepository
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class URLTitleOperationTests {
    @Autowired lateinit var urlTitleOperation: URLTitleOperation
    @Autowired lateinit var urlCommandsOperation: ModifyIgnoredUrlsRouterOperation
    @Autowired lateinit var ignoredHostRepository: IgnoredHostRepository

    @Test
    fun `3-reject messages from invalid sources`() {
        assertFalse(
            urlTitleOperation.canHandle(
                routerMessage {
                    content = "https://enigmastation.com/"
                    messageSource = MessageSource.DISCORD
                }
            )
        )
        assertTrue(
            urlTitleOperation.canHandle(
                routerMessage {
                    content = "https://enigmastation.com/"
                    messageSource = MessageSource.IRC
                }
            )
        )
    }

    @Test
    fun `17-list-commands-test`() {
        val response =
            urlCommandsOperation.handleMessage(routerMessage { content = "~url ignore list" })
        assertNotNull(response)
        assertTrue(response.content.contains("Banned hosts include: "))
        assertTrue(response.content.contains(" x.com"))
    }

    @Test
    fun `17-add-banned-host-test`() {
        var ignoredHost = ignoredHostRepository.findByHostNameIgnoreCaseStartsWith("foo.bar.com")
        assertTrue(ignoredHost.isEmpty)
        val response =
            urlCommandsOperation.handleMessage(
                routerMessage { content = "~url ignore add foo.bar.com" }
            )
        assertNotNull(response)
        assertTrue(response.content.isEmpty())
        assertEquals(MessageScope.TERMINAL, response.scope)
        ignoredHost = ignoredHostRepository.findByHostNameIgnoreCaseStartsWith("/foo.bar.com")
        assertTrue(ignoredHost.isPresent)
    }

    @Test
    fun `17-delete-banned-host-test`() {
        var ignoredHost = ignoredHostRepository.findByHostNameIgnoreCaseStartsWith("foo.baz.com")
        assertTrue(ignoredHost.isEmpty)
        var response =
            urlCommandsOperation.handleMessage(
                routerMessage { content = "~url ignore add foo.baz.com" }
            )
        assertNotNull(response)
        assertTrue(response.content.isEmpty())
        assertEquals(MessageScope.TERMINAL, response.scope)
        ignoredHost = ignoredHostRepository.findByHostNameIgnoreCaseStartsWith("/foo.baz.com")
        assertTrue(ignoredHost.isPresent)
        response =
            urlCommandsOperation.handleMessage(
                routerMessage { content = "~url ignore delete foo.baz.com" }
            )
        assertNotNull(response)
        assertTrue(response.content.isEmpty())
        assertEquals(MessageScope.TERMINAL, response.scope)
        ignoredHost = ignoredHostRepository.findByHostNameIgnoreCaseStartsWith("/foo.baz.com")
        assertTrue(ignoredHost.isEmpty)
    }
}
