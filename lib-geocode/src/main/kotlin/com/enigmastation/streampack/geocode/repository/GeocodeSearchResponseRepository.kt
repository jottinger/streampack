/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.repository

import com.enigmastation.streampack.geocode.model.GeocodeSearchResponse
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GeocodeSearchResponseRepository : JpaRepository<GeocodeSearchResponse, UUID> {
    fun findBySearchStringIgnoreCase(query: String): Optional<GeocodeSearchResponse>
}
