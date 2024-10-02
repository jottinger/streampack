/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.htmlDecode
import com.enigmastation.streampack.extensions.htmlEncode
import com.enigmastation.streampack.extensions.isChannelReference
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.pluralize
import com.enigmastation.streampack.extensions.possessive
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class UtilityTests {
    @Test
    fun testDecode() {
        assertEquals("\"Hi there!\"", "&quot;Hi there!&quot;".htmlDecode())
    }

    @Test
    fun testEncode() {
        assertEquals("&quot;Hi there!&quot;", "\"Hi there!\"".htmlEncode())
    }

    @ParameterizedTest
    @MethodSource("compressData")
    fun testCompress(expected: String, input: String) {
        assertEquals(expected, input.compress())
    }

    @ParameterizedTest
    @MethodSource("joinToStringWithAndData")
    fun testJoinToStringWithAndData(expected: String, input: List<String>) {
        assertEquals(expected, input.joinToStringWithAnd())
    }

    @Test
    fun testChannelReference() {
        assertTrue("#foo".isChannelReference())
        assertTrue(" #foo".isChannelReference())
        assertFalse("foo".isChannelReference())
    }

    @Test
    fun testPossessive() {
        assertEquals("bean's", "bean".possessive())
        assertEquals("sharks'", "sharks".possessive())
    }

    @ParameterizedTest
    @MethodSource("pluralizationData")
    fun testPluralize(expected: String, noun: String, list: Collection<Any>) {
        var output = noun.pluralize(list)
        assertEquals(expected, output)
    }

    companion object {
        @JvmStatic
        fun compressData(): Stream<Arguments> =
            Stream.of(
                Arguments.of("hi there", "hi there"),
                Arguments.of("hi there", "hi there   "),
                Arguments.of("hi there", "   hi there"),
                Arguments.of("hi there", "hi   there"),
                Arguments.of("hi there", "  hi   there  "),
            )

        @JvmStatic
        fun joinToStringWithAndData(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", emptyList<String>()),
                Arguments.of("one", listOf("one")),
                Arguments.of("one and two", listOf("one", "two")),
                Arguments.of("one, two, and three", listOf("one", "two", "three")),
                Arguments.of("one, two, three, and four", listOf("one", "two", "three", "four"))
            )

        @JvmStatic
        fun pluralizationData(): Stream<Arguments> =
            Stream.of(
                Arguments.of("things", "things", listOf("1", "2")),
                Arguments.of("things", "thing", listOf("1", "2")),
                Arguments.of("thing", "things", listOf("1")),
                Arguments.of("thing", "thing", listOf("1")),
                Arguments.of("things", "things", emptyList<Any>()),
                Arguments.of("things", "thing", emptyList<Any>()),
                // now the fun stuff
                Arguments.of("irises", "iris", listOf("1", "2")),
                Arguments.of("irises", "irises", listOf("1", "2")),
                Arguments.of("iris", "iris", listOf("1")),
                Arguments.of("iris", "irises", listOf("1")),
                Arguments.of("wolf", "wolf", listOf("1")),
                Arguments.of("wolves", "wolf", listOf("1", "2")),
            )
    }
}
