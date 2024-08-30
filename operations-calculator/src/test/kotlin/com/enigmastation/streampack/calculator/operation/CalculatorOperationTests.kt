/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.util.stream.Stream
import kotlin.test.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CalculatorOperationTests {
    @Autowired lateinit var calculatorOperation: CalculatorOperation

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
