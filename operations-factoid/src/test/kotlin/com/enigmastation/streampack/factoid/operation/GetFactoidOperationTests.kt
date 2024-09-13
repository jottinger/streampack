/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GetFactoidOperationTests {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired lateinit var setFactoidOperation: SetFactoidOperation

    @Autowired lateinit var getFactoidOperation: GetFactoidOperation

    @Test
    fun `test retrieving factoid attributes`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo.text=bar" })
        setFactoidOperation.handleMessage(
            routerMessage { content = "~foo.url=https://enigmastation.com" }
        )
        setFactoidOperation.handleMessage(routerMessage { content = "~foo.tags=baz,bletch" })
        setFactoidOperation.handleMessage(routerMessage { content = "~foo.languages=java" })
        setFactoidOperation.handleMessage(routerMessage { content = "~foo.type=project" })

        assertEquals(
            "foo is bar.",
            getFactoidOperation.handleMessage(routerMessage { content = "~foo.text" })?.content
        )
        // test case inssensitivity
        assertEquals(
            "foo is bar.",
            getFactoidOperation.handleMessage(routerMessage { content = "~Foo.text" })?.content
        )
        var value =
            (getFactoidOperation.handleMessage(routerMessage { content = "~foo.info" })?.content
                ?: "")
        logger.info("{}", value)
        assertTrue(value.contains("text, url, tags, language, and type"))
        logger.info("issuing unknown")
        value =
            (getFactoidOperation.handleMessage(routerMessage { content = "~foo" })?.content ?: "")
        logger.info("unknown: {}", value)
        assertNull(getFactoidOperation.handleMessage(routerMessage { value = "~bar.text" }))
        setFactoidOperation.handleMessage(
            routerMessage { content = "~foo.urls=https://enigmastation.com,https://primatejs.com" }
        )
        value = getFactoidOperation.handleMessage(routerMessage { content = "~foo" })?.content ?: ""
        logger.info("value for ~foo: {}", value)
        assertTrue(value.contains("URLs"))
    }

    @Test
    fun `28-test seealso interpolation`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo1=bar" })
        setFactoidOperation.handleMessage(routerMessage { content = "~foo2=baz" })
        setFactoidOperation.handleMessage(routerMessage { content = "~foo3.seealso=foo1,foo2" })
        var message = getFactoidOperation.handleMessage(routerMessage { content = "~foo3" })
        assertEquals("See also: ~foo1 and ~foo2", message?.content)
        message = getFactoidOperation.handleMessage(routerMessage { content = "~foo3.seealso" })
        assertEquals("See also: ~foo1 and ~foo2", message?.content)
    }

    @ParameterizedTest
    @MethodSource("interpolationTestData")
    fun `20-factoid interpolation`(command: String, input: String, output: String?) {
        setFactoidOperation.handleMessage(routerMessage { content = command })
        val result = getFactoidOperation.handleMessage(routerMessage { content = input })
        assertEquals(output, result?.content)
    }

    companion object {
        @JvmStatic
        fun interpolationTestData() =
            Stream.of(
                Arguments.of("~foo8=foo", "~foo8", "foo8 is foo."),
                Arguments.of("~foo8=foo $1", "~foo8 bar", "foo8 is foo bar."),
                Arguments.of("~foo8=foo $1 $2", "~foo8 bar baz", "foo8 is foo bar baz."),
                Arguments.of("~foo8=foo $1 $2 $2", "~foo8 bar baz", "foo8 is foo bar baz baz."),
                Arguments.of("~foo8=foo $1 $2 $1", "~foo8 bar baz", "foo8 is foo bar baz bar."),
                Arguments.of(
                    "~foo8=foo $1 $2",
                    "~foo8 bar",
                    "foo8: Not enough arguments to replace placeholders. Expected at least 2 but got 1."
                ),
                Arguments.of(
                    "~foo8=foo $1",
                    "~foo8",
                    "foo8: Not enough arguments to replace placeholders. Expected at least 1 but got 0."
                ),
            )
    }
}
