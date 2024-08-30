/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import com.enigmastation.streampack.factoid.repository.FactoidAttributeRepository
import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SetFactoidOperationTests {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired lateinit var setFactoidOperation: SetFactoidOperation

    @Autowired lateinit var getFactoidOperation: GetFactoidOperation

    @Autowired lateinit var factoidAttributeRepository: FactoidAttributeRepository

    @BeforeEach
    fun clearDb() {
        factoidAttributeRepository.deleteAll()
    }

    @Test
    fun `ignore unknown attribute type`() {
        val command = setFactoidOperation.parseInput("~foo.unknown=bar")
        // unknown attribute type is not allowed to be set!
        assertNull(command)
    }

    @ParameterizedTest
    @MethodSource("setFactoidStrings")
    fun `parse out set factoid commands`(
        input: String,
        selector: String,
        attribute: String,
        value: String
    ) {
        val command = setFactoidOperation.parseInput(input)
        println(command)
        val attr = FactoidAttributeType.valueOf(attribute.uppercase())
        assertEquals(SetFactoidCommand(selector, attr, value), command)
    }

    @ParameterizedTest
    @MethodSource("setFactoidStrings")
    fun `test save factoid commands`(
        input: String,
        selector: String,
        attribute: String,
        value: String
    ) {
        setFactoidOperation.handleMessage(routerMessage { content = input })
        val factoid =
            factoidAttributeRepository.findBySelectorIgnoreCaseAndAttributeType(
                selector,
                FactoidAttributeType.valueOf(attribute.uppercase())
            )
        assertTrue(factoid.isPresent)
        assertEquals(value, factoid.get().attributeValue)
    }

    @Test
    fun `test removing tags with selector names`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo=<reply>foo!" })
        setFactoidOperation.handleMessage(routerMessage { content = "~foo.seealso=foo" })
        var response = getFactoidOperation.handleMessage(routerMessage { content = "~foo" })
        assertNotNull(response)
        assertEquals("foo!", response.content)
    }

    @Test
    fun `test forgetting factoids`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo1=bar" })
        var response = getFactoidOperation.handleMessage(routerMessage { content = "~foo1" })
        println(response)
        assertEquals("foo1 is bar.", response?.content)
        // forget is a GET service, not a SET service, because of syntax: SET uses "=" and GET
        // does not
        assertNull(getFactoidOperation.handleMessage(routerMessage { content = "~foo1.forget" }))
        response = getFactoidOperation.handleMessage(routerMessage { content = "~foo1" })
        assertNull(response)
    }

    @Test
    fun `validate setting factoids is also case insensitive`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo1=bar" })
        setFactoidOperation.handleMessage(routerMessage { content = "~FOO1=bar" })
        var response = getFactoidOperation.handleMessage(routerMessage { content = "~FOO1" })
        logger.info("response: {}", response)
        assertEquals("foo1 is bar.", response?.content)
    }

    @Test
    fun `validate that reply prefix is case-insensitive`() {
        setFactoidOperation.handleMessage(routerMessage { content = "~foo1=<REPLY>bar" })
        var response = getFactoidOperation.handleMessage(routerMessage { content = "~FOO1" })
        assertEquals("bar.", response?.content)
    }

    @ParameterizedTest
    @MethodSource("setAndQuery")
    fun `test set and query`(set: String, get: String, result: String) {
        setFactoidOperation.handleMessage(routerMessage { content = set })
        var response = getFactoidOperation.handleMessage(routerMessage { content = get })
        assertEquals(result, response?.content)
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun setFactoidStrings(): Stream<Arguments> =
            Stream.of(
                Arguments.of("~this is the time=now", "this is the time", "text", "now"),
                Arguments.of(
                    "~this is the time.url=https://time.com",
                    "this is the time",
                    "urls",
                    "https://time.com"
                ),
                Arguments.of(
                    "~this.that.the other.url=https://blaz.com",
                    "this.that.the other",
                    "urls",
                    "https://blaz.com"
                ),
                Arguments.of("~this.that.theother=bar", "this.that.theother", "text", "bar"),
                Arguments.of("~this.that.theother.text=bar", "this.that.theother", "text", "bar"),
                Arguments.of(
                    "~this.that.theother.text=bar    baz",
                    "this.that.theother",
                    "text",
                    "bar baz"
                ),
                Arguments.of("~this    that.url=bar    baz", "this that", "urls", "bar baz"),
                Arguments.of("~this that.text=", "this that", "text", "")
            )

        @Suppress("unused")
        @JvmStatic
        fun setAndQuery(): Stream<Arguments> =
            Stream.of(
                Arguments.of("~foo=bar", "~foo", "foo is bar."),
                Arguments.of("~foo=<reply>bar", "~foo", "bar."),
                Arguments.of("~foo=<reply>bar", "~foo.text", "bar."),
                Arguments.of("~foo=bar.", "~foo", "foo is bar."),
                Arguments.of("~foo=bar!", "~foo", "foo is bar!"),
                Arguments.of("~I.am=a baby", "~I.am", "i.am is a baby."),
            )
    }
}
