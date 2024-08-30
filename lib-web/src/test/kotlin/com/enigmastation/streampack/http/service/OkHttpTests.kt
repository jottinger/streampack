/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.http.service

import com.enigmastation.streampack.web.service.OkHttpService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OkHttpTests {
    @LocalServerPort var port: Int = 0

    @Autowired lateinit var okHttpService: OkHttpService

    @Test
    fun `context loads`() {
        assertTrue(port != 0)
        assertNotNull(okHttpService)
    }

    @Test
    fun `make sure get service works`() {
        val testThing =
            okHttpService.get("http://localhost:$port/foo/3", klass = TestThing::class.java)
        println(testThing)
        assertEquals(3, testThing.value)
    }
}
