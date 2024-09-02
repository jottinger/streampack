/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summarize.service

import org.junit.jupiter.api.Tag
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("integration")
class SummarizeClientTests {
    @Autowired lateinit var service: SummarizeService

    @Test
    fun `context loads`() {
        println(service)
    }

    @Test
    fun `test summarization`() {
        println(
            service.summarizeURL(
                "https://enigmastation.com/2024/08/21/i-was-judging-rust-unfairly/"
            )
        )
    }

    @Test
    fun `test summarization with bad url`() {
        assertThrows<Throwable> {
            println(
                service.summarizeURL(
                    "https://enigmastation.casdom/2024/08/21/i-was-judging-rust-unfairly/"
                )
            )
        }
    }
}
