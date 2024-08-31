/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ListOperationsOperationTests {
    @Autowired lateinit var listOperationsOperation: ListOperationsOperation

    @Test
    fun `test list operations canHandle failure`() {
        assertFalse(listOperationsOperation.canHandle(routerMessage { content = "list" }))
        assertFalse(listOperationsOperation.canHandle(routerMessage { content = "~list" }))
    }

    @Test
    fun `test list operations canHandle passing`() {
        assertTrue(
            listOperationsOperation.canHandle(routerMessage { content = "~list operations" })
        )
    }

    @Test
    fun `test failure from handleMessage`() {
        assertNull(listOperationsOperation.handleMessage(routerMessage { content = "list" }))
    }

    @Test
    fun `description is not empty`() {
        assertFalse(listOperationsOperation.description().isEmpty())
    }

    @Test
    fun `longDescription is empty`() {
        // we'll probably want to have a long description here eventually, eh
        assertEquals(
            "No long description for ListOperationsOperation",
            listOperationsOperation.longDescription()
        )
    }
}
