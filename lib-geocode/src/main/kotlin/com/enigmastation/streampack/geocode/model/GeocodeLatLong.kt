/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GeocodeLatLong(
    @JsonProperty("lat") var latitude: Double,
    @JsonProperty("lng") var longitude: Double
)
