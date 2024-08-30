/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Geocode(
    @JsonProperty("formatted_address") var formattedAddress: String? = null,
    var geometry: GeocodeGeometry? = null
) {
    fun latitude() = geometry?.location?.latitude

    fun longitude() = geometry?.location?.longitude
}
