/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "geocode_search", indexes = [Index(columnList = "searchString", unique = true)])
class GeocodeSearchResponse(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    var searchString: String? = null,
    var formattedAddress: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    override fun toString(): String {
        return "GeocodeSearchResponse[id=$id,searchString='$searchString',formattedAddress='$formattedAddress',latitude=$latitude,longitude=$longitude]"
    }
}
