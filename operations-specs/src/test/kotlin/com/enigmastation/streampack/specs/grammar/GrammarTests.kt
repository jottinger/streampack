/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.grammar

import com.enigmastation.streampack.specs.model.SpecsRequest
import com.enigmastation.streampack.specs.model.SpecsRequestType
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class GrammarTests {
    @ParameterizedTest
    @MethodSource("grammar inputs")
    fun `test grammar`(input: String, expected: SpecsRequest?) {
        val parser = SpecsGrammar.parser()
        val result = parser.run(input)
        if (expected != null) {
            assertEquals(expected, result.stackTop)
            assertTrue(result.matchedEntireInput)
        } else {
            assertNull(result.stackTop)
        }
    }

    companion object {
        @JvmStatic
        fun `grammar inputs`() =
            Stream.of(
                Arguments.of("~rfc 2812", SpecsRequest(SpecsRequestType.RFC, 2812)),
                Arguments.of("~rfc2812", SpecsRequest(SpecsRequestType.RFC, 2812)),
                Arguments.of("~rfc2812  ", SpecsRequest(SpecsRequestType.RFC, 2812)),
                Arguments.of("~   rfc2812  ", SpecsRequest(SpecsRequestType.RFC, 2812)),
                Arguments.of("~rfc -1", null),
                Arguments.of("~jep 281", SpecsRequest(SpecsRequestType.JEP, 281)),
                Arguments.of("~    jep 281", SpecsRequest(SpecsRequestType.JEP, 281)),
                Arguments.of("~    jep   281   ", SpecsRequest(SpecsRequestType.JEP, 281)),
                Arguments.of("~    jeep   281   ", null),
            )
    }
}
