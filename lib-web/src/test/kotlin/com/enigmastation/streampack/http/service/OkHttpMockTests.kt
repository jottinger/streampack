/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.http.service

import com.enigmastation.streampack.web.service.OkHttpService
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OkHttpMockTests {
    lateinit var okHttpService: OkHttpService

    @Test
    fun `test that call does not go out`() {
        okHttpService = mockk<OkHttpService>(relaxed = true)
        every { okHttpService.get(any(), any(), any(), TestThing::class.java) } returns
            TestThing(17)

        var thing = okHttpService.get("http://foo.bar.notreal", klass = TestThing::class.java)
        assertEquals(17, thing.value)
    }
}
