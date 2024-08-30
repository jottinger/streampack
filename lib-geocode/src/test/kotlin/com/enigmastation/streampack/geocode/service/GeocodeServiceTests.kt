/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.service

import com.enigmastation.streampack.geocode.model.Geocode
import com.enigmastation.streampack.geocode.model.GeocodeGeometry
import com.enigmastation.streampack.geocode.model.GeocodeLatLong
import com.enigmastation.streampack.geocode.model.GeocodeResult
import com.enigmastation.streampack.web.service.OkHttpService
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GeocodeServiceTests {
    @Autowired lateinit var service: GeocodeService
    lateinit var okHttpService: OkHttpService

    @BeforeTest
    fun `set up mock`() {
        okHttpService = mockk<OkHttpService>(relaxed = true)
        every { okHttpService.get(any(), any(), any(), GeocodeResult::class.java) } returns
            GeocodeResult(
                listOf(
                    Geocode("Tallahassee, FL, USA", GeocodeGeometry(GeocodeLatLong(30.438, -84.28)))
                ),
                "OK"
            )
        service.okHttpService = okHttpService
    }

    @Test
    fun `test geocode service`() {
        var code = service.getGeocode("Tallahassee, FL")
        assertTrue(code.isPresent)
        val actual = code.get()
        assertEquals(30.438.toDouble(), actual.latitude!!.toDouble(), 0.01)
        assertEquals((-84.28).toDouble(), actual.longitude!!.toDouble(), 0.01)
    }

    @Test
    fun `test geocode service part 2`() {
        var code = service.getGeocode("Tallahassee, FL")
        assertTrue(code.isPresent)
        val actual = code.get()
        assertEquals(30.438.toDouble(), actual.latitude!!.toDouble(), 0.01)
        assertEquals((-84.28).toDouble(), actual.longitude!!.toDouble(), 0.01)
    }
}
