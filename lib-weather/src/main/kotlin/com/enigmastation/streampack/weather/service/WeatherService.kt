/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.service

import com.enigmastation.streampack.weather.model.WeatherResponse
import com.enigmastation.streampack.web.service.OkHttpService
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@ConfigurationProperties("streampack.weather")
@Service
class WeatherService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    lateinit var apiKey: String
    private val WEATHER_URL =
        "https://api.openweathermap.org/data/3.0/onecall?units=metric&exclude=minutely,hourly,daily"
    @Autowired lateinit var okHttpService: OkHttpService

    fun getWeatherForLatLong(lat: Double, long: Double): Optional<WeatherResponse> {
        var data =
            okHttpService.get(
                WEATHER_URL,
                parameters = mapOf("appid" to apiKey, "lat" to lat, "lon" to long),
                klass = WeatherResponse::class.java
            )
        return Optional.ofNullable(data)
    }
}
