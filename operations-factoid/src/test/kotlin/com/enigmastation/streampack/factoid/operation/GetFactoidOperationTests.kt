/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
}
