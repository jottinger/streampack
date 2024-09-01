/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.weather.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.weather")
@Component
class WeatherConfiguration {
    var apiKey: String = ""
}
