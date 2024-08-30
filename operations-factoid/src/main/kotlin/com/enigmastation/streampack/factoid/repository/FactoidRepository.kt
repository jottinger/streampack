/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.repository

import com.enigmastation.streampack.factoid.entity.Factoid
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface FactoidRepository : JpaRepository<Factoid, UUID> {
    fun findBySelectorIgnoreCase(selector: String): Optional<Factoid>
}
