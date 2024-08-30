/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
class FactoidDTO(
    var id: UUID? = null,
    var selector: String,
    var text: String? = null,
    var tags: String? = null,
    var urls: String? = null,
    var seealso: String? = null,
    var languages: String? = null,
    var updatedBy: String? = null,
    var updateTimestamp: OffsetDateTime? = null
)
