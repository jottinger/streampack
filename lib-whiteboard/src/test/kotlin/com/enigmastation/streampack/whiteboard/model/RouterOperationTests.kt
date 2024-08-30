/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals

class RouterOperationTests {
    @Test
    fun `test prioritization`() {
        val operations =
            listOf(
                object : RouterOperation(priority = 1) {},
                object : RouterOperation(priority = 10) {},
                object : RouterOperation(priority = 3) {},
            )
        val sorted = operations.sorted().map { it.priority }.toList()
        assertEquals(1, sorted[0])
        assertEquals(10, sorted[2])
    }
}
