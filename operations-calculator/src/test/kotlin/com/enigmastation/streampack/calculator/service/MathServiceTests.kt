/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MathServiceTests {
    @Autowired lateinit var service: MathService

    @Test
    fun `math is evaluated`() {
        val expression = "(42/3.14)*4"
        println(service.evaluate(expression))
    }

    @Test
    fun `more math fun`() {
        val expression = "87238.0/127.17"
        println(service.evaluate(expression))
    }
}
