/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.geocode")
@Component
class GeocodeConfiguration {
    var googleApiKey: String = ""
}
