/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.model

data class GeocodeResult(var results: List<Geocode> = listOf(), var status: String? = null)
