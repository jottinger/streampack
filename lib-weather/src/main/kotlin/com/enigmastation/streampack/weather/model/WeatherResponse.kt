/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.model

data class WeatherResponse(
    val lat: Double? = null,
    val lon: Double? = null,
    val current: WeatherState? = null
)
