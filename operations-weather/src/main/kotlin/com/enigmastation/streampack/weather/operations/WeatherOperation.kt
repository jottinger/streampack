/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.operations

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.geocode.service.GeocodeService
import com.enigmastation.streampack.weather.service.WeatherService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class WeatherOperation(val geocodeService: GeocodeService, val weatherService: WeatherService) :
    RouterOperation() {
    override fun description(): String {
        return "~weather [location] will query OpenWeatherMap for that location; if it's discoverable, the current weather will be shown."
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("~weather ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val locationText = message.content.removePrefix("~weather ").compress()
        val location = geocodeService.getGeocode(locationText)
        return if (location.isPresent) {
            val loc = location.get()
            val weather = weatherService.getWeatherForLatLong(loc.latitude!!, loc.longitude!!)
            if (weather.isPresent) {
                val w = weather.get()
                message.respondWith(
                    "The weather for ${loc.formattedAddress} is ${w.current?.temp}C (${fahrenheit(w.current?.temp!!)}F), and is described as \"${w.current?.weather?.firstOrNull()?.description}\""
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun fahrenheit(c: Double): Double {
        return (c * 9 / 5) + 32.0
    }
}
