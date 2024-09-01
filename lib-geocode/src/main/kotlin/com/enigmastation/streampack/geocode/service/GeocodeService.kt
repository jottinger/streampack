/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.geocode.service

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.htmlEncode
import com.enigmastation.streampack.geocode.model.GeocodeResult
import com.enigmastation.streampack.geocode.model.GeocodeSearchResponse
import com.enigmastation.streampack.geocode.repository.GeocodeSearchResponseRepository
import com.enigmastation.streampack.web.service.OkHttpService
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GeocodeService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val GEOCODEENDPOINT = "https://maps.googleapis.com/maps/api/geocode/json"

    @Autowired lateinit var geocodeConfiguration: GeocodeConfiguration
    @Autowired lateinit var okHttpService: OkHttpService

    @Autowired private lateinit var repository: GeocodeSearchResponseRepository

    fun getGeocode(location: String): Optional<GeocodeSearchResponse> {
        // TODO what happens in failures? What about badly formed geocodes? Do they ever happen?
        val earlyResponse = repository.findBySearchStringIgnoreCase(location.compress())
        return if (earlyResponse.isPresent) {
            earlyResponse
        } else {
            var data =
                okHttpService.get(
                    GEOCODEENDPOINT,
                    mapOf(
                        "address" to location.htmlEncode(),
                        "key" to geocodeConfiguration.googleApiKey
                    ),
                    klass = GeocodeResult::class.java
                )
            val result = data.results
            require(result.isNotEmpty()) {
                throw IllegalArgumentException("Geocode retrieval failed: $data")
            }
            val datum = result.first()
            logger.debug("Caching geocode for {}", result)
            Optional.of(
                repository.save(
                    GeocodeSearchResponse(
                        searchString = location.compress(),
                        formattedAddress = datum.formattedAddress,
                        latitude = datum.latitude(),
                        longitude = datum.longitude()
                    )
                )
            )
        }
    }
}
