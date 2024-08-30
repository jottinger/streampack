/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherState(
    val temp: Double? = null,
    @JsonProperty("feels_like") val feelsLike: Double? = null,
    val weather: List<WeatherDescription>? = null
)
