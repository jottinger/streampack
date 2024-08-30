/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack

import com.enigmastation.streampack.extensions.endsWithPunctuation
import java.util.stream.Stream
import kotlin.test.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ExtensionsTests {
    @ParameterizedTest
    @MethodSource("punctuationInputs")
    fun testPunctuation(input: String, endsWithPunctuation: Boolean) {
        assertEquals(endsWithPunctuation, input.endsWithPunctuation())
    }

    companion object {
        @JvmStatic
        fun punctuationInputs() =
            Stream.of(
                Arguments.of("hi there", false),
                Arguments.of("hi there!", true),
                Arguments.of("hi there?", true),
                Arguments.of("hi there.", true),
                Arguments.of("hi there;", true),
                Arguments.of("hi there:", true),
                Arguments.of("hi there*", true),
                Arguments.of("\"hi there!\"", true),
            )
    }
}
