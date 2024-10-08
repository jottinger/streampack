/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summarize.operation

import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.routerMessage
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("integration")
class SummarizeOperationTests {
    @Autowired lateinit var operation: SummarizeRouterOperation

    //    @Test
    fun `test valid url`() {
        val result =
            operation.handleMessage(
                routerMessage {
                    content =
                        "~summarize https://enigmastation.com/2024/08/21/i-was-judging-rust-unfairly/"
                    messageSource = MessageSource.IRC
                }
            )
        println(result)
    }
}
