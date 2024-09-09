/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CalculatorOperationTests {
    @Autowired lateinit var calculatorOperation: CalculatorOperation

    @Test
    fun `test parser match`() {
        val parser = CalcOperationGrammar.parser()
        val result = parser.run("~calc 126 + 12 / 2(4)")
        assertTrue(result.matched)
        assertEquals("126+12/2(4)", result.stackTop)
    }

    @Test
    fun `test parser fail`() {
        val parser = CalcOperationGrammar.parser()
        val result = parser.run("~calc ")
        assertFalse(result.matched)
        assertNull(result.stackTop)
    }

    @ParameterizedTest
    @MethodSource("legalExpressions")
    fun `test valid expressions`(expression: String) {
        val response =
            calculatorOperation.handleMessage(routerMessage { content = "~calc $expression" })
        println(response)
        assertNotNull(response)
    }

    companion object {
        @JvmStatic
        fun legalExpressions() =
            Stream.of(
                Arguments.of("42/3"),
                Arguments.of("87238.0/127.17"),
                Arguments.of("2^4"),
                Arguments.of("sin(pi/3)")
            )
    }
}
