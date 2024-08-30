/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard

import com.enigmastation.streampack.extensions.htmlDecode
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilityTests {
    @Test
    fun testDecode() {
        assertEquals("\"Hi there!\"", "&quot;Hi there!&quot;".htmlDecode())
    }
}
