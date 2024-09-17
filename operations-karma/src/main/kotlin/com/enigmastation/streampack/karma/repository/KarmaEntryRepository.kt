/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.repository

import com.enigmastation.streampack.karma.entity.KarmaEntry
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KarmaEntryRepository : JpaRepository<KarmaEntry, UUID> {
    fun findKarmaEntryByCreateTimestampBefore(creationDate: OffsetDateTime): List<KarmaEntry>

    fun findKarmaEntryBySelectorOrderByCreateTimestampDesc(name: String): List<KarmaEntry>

    fun findTop1BySelector(selector: String): List<KarmaEntry>
}
