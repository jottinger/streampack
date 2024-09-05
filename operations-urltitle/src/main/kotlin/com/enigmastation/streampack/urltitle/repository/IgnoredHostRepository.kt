/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.repository

import com.enigmastation.streampack.urltitle.entity.IgnoredHost
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IgnoredHostRepository : JpaRepository<IgnoredHost, UUID> {
    fun findByHostNameIgnoreCaseStartsWith(hostName: String): Optional<IgnoredHost>
}
