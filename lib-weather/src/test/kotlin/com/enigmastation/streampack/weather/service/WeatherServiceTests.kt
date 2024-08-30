/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.service

import com.enigmastation.streampack.geocode.model.Geocode
import com.enigmastation.streampack.geocode.model.GeocodeGeometry
import com.enigmastation.streampack.geocode.model.GeocodeLatLong
import com.enigmastation.streampack.geocode.model.GeocodeResult
import com.enigmastation.streampack.geocode.service.GeocodeService
import com.enigmastation.streampack.weather.model.WeatherDescription
import com.enigmastation.streampack.weather.model.WeatherResponse
import com.enigmastation.streampack.weather.model.WeatherState
import com.enigmastation.streampack.web.service.OkHttpService
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class WeatherServiceTests {
    @Autowired lateinit var service: WeatherService
    @Autowired lateinit var geocode: GeocodeService
    lateinit var okHttpService: OkHttpService

    @BeforeTest
    fun `set up okhttp`() {
        okHttpService = mockk<OkHttpService>()
        every { okHttpService.get(any(), any(), any(), GeocodeResult::class.java) } returns
            GeocodeResult(
                listOf(
                    Geocode("Tallahassee, FL, USA", GeocodeGeometry(GeocodeLatLong(30.438, -84.28)))
                ),
                "OK"
            )
        every { okHttpService.get(any(), any(), any(), WeatherResponse::class.java) } returns
            WeatherResponse(
                30.438,
                -84.28,
                WeatherState(27.8, 29.0, listOf(WeatherDescription("sunny", "clear skies")))
            )
        service.okHttpService = okHttpService
        geocode.okHttpService = okHttpService
    }

    @Test
    fun `get weather for Tallahassee`() {
        val datum = geocode.getGeocode("tallahassee, fl").orElseThrow()
        val weather = service.getWeatherForLatLong(datum.latitude!!, datum.longitude!!)
        println(weather)
    }
}
