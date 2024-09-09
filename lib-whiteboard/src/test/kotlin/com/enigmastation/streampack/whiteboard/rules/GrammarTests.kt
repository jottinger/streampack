/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.rules

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class GrammarTests {
    val wordUrlWordGrammar = WordUrlWordGrammar::class.createGrammar()
    val wordUrlWordParser = Parser(wordUrlWordGrammar)
    val urlGrammar = UrlGrammar::class.createGrammar()
    val wordUrlParser = Parser(urlGrammar)
    val channelGrammar = ChannelGrammar::class.createGrammar()
    val channelParser = Parser(channelGrammar)

    @ParameterizedTest
    @MethodSource("providedContent")
    fun `test url detection rule`(input: String, result: Boolean, match: String?) {
        // detect "word http://foo.com word" - whitespace, a word, whitespace, a url, whitespace, a
        // word, whitespace
        var parseResult = wordUrlWordParser.run(input)
        assertEquals(result, parseResult.matched)
        if (result) {
            assertEquals(match, parseResult.stackTop)
        }
    }

    @Test
    fun `validate that urls can end content`() {
        val result = wordUrlParser.run("http://foo.com")
        assertTrue(result.matched)
        assertEquals("http://foo.com", result.stackTop)
    }

    @Test
    fun `validate channel parsing rule`() {
        val result = channelParser.run("http://foo.com bar #foo #baz-foo")
        assertTrue(result.matched)
        val results = result.stackTop
        assertNotNull(results)
        assertEquals(2, results.size)
        println(result.stackTop.toString())
    }

    companion object {
        @JvmStatic
        fun providedContent() =
            Stream.of(
                Arguments.of("foo bar baz", false, null),
                Arguments.of("foo http://boo.com baz", true, "http://boo.com"),
                Arguments.of("foo https://boo.com baz", true, "https://boo.com"),
                Arguments.of(" foo   https://boo.com baz   ", true, "https://boo.com"),
                Arguments.of("   foo   https://boo.com baz   ", true, "https://boo.com"),
            )
    }
}
