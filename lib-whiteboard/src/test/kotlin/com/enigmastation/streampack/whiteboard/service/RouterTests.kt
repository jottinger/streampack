/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.routerMessage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
class RouterTests {
    @Autowired lateinit var router: Router
    @Autowired lateinit var helloRouterOperation: HelloRouterOperation
    @Autowired lateinit var helloService: HelloService

    @Autowired lateinit var otherService: OtherService

    @Test
    @Order(2)
    fun `services loaded`() {
        // if this is false, we have nothing to test
        assertTrue(router.services.isNotEmpty())
    }

    @Test
    @Order(3)
    fun `transformers loaded`() {
        // if this is false, we have nothing to test
        assertTrue(router.routerOperations.isNotEmpty())
        println(router.routerOperations)
    }

    @Test
    @Order(4)
    fun `propagate event from hello source`() {
        otherService.lastMessage = null
        helloService.lastMessage = null
        helloService.fireEvent()
        // remember: dispatch is async! We need to give it SOME time to fire. 5 is a magic number
        // for github actions.
        watchForTimeout({ otherService.lastMessage })
        assertEquals("hi there", otherService.lastMessage?.content)
    }

    @Test
    @Order(5)
    fun `propagate event from hello source with rudeness`() {
        helloService.lastMessage = null
        otherService.lastMessage = null
        helloService.fireEvent("hello, darn it")
        // remember: dispatch is async! We need to give it SOME time to fire.
        watchForTimeout({ otherService.lastMessage })
        assertEquals("hi there, d**n it", otherService.lastMessage?.content)
    }

    @Test
    @Order(6)
    fun `propagate event across services`() {
        otherService.lastMessage = null
        helloService.fireEvent("!otherService")
        // remember: dispatch is async! We need to give it SOME time to fire.
        watchForTimeout({ otherService.lastMessage })
        assertNotNull(otherService.lastMessage)
        assertEquals("!otherService", otherService.lastMessage?.content)
    }

    @Test
    @Order(7)
    fun `test list operations operation`() {
        otherService.lastMessage = null
        router.dispatch(routerMessage { content = "~list operations" })
        watchForTimeout({ otherService.lastMessage })
        Thread.sleep(50)
        assertNotNull(otherService.lastMessage)
        assertEquals(
            "Installed operations are: SampleHelloTransformer, HelpOperation, and ListOperationsOperation",
            otherService.lastMessage?.content
        )
    }

    @Test
    @Order(7)
    fun `test help operation`() {
        otherService.lastMessage = null
        router.dispatch(routerMessage { content = "~help   SampleHelloTransformer" })
        watchForTimeout({ otherService.lastMessage })
        Thread.sleep(50)
        assertNotNull(otherService.lastMessage)
        assertEquals(
            "${helloRouterOperation.name}: ${helloRouterOperation.description()}",
            otherService.lastMessage?.content
        )
    }

    fun watchForTimeout(thing: () -> RouterMessage?, timeout: Int = 5000) {
        val start = System.currentTimeMillis()
        while (thing() == null && (System.currentTimeMillis() - start) < timeout) {
            Thread.sleep(50) // Avoid busy-waiting; check every 100ms
        }
        println("Delay: ${System.currentTimeMillis() - start} ms, value: ${thing()}")
    }
}
