/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpecsTests {
    @Autowired lateinit var rfcRouterOperation: SpecsRouterOperation

    @Test fun `context loads`() {}

    @ParameterizedTest
    @MethodSource("rfc tests")
    fun testOperations(input: String, title: String?) {
        val response = rfcRouterOperation.handleMessage(routerMessage { content = input })
        if (title == null) {
            assertNull(response)
        } else {
            assertEquals(title, response?.content)
        }
    }

    companion object {
        @JvmStatic
        fun `rfc tests`() =
            Stream.of(
                Arguments.of(
                    "~rfc 2812",
                    "RFC 2812: Internet Relay Chat: Client Protocol (https://www.rfc-editor.org/rfc/rfc2812.html)"
                ),
                Arguments.of(
                    "~rfc2812",
                    "RFC 2812: Internet Relay Chat: Client Protocol (https://www.rfc-editor.org/rfc/rfc2812.html)"
                ),
                Arguments.of("~rfc28122", null),
                Arguments.of(
                    "~jsr3",
                    "JSR 3: Java Management Extensions (JMX) Specification (https://jcp.org/en/jsr/detail?id=3)"
                ),
                Arguments.of("~jep 3", "JEP 3: JDK Release Process (https://openjdk.org/jeps/3)"),
                Arguments.of("~rfc 0", null)
            )
    }
}
