/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GeocodeGeometry(@JsonProperty("location") val location: GeocodeLatLong? = null)
