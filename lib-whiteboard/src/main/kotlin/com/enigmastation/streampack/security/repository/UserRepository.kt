/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.security.repository

import com.enigmastation.streampack.security.entity.RouterUser
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<RouterUser, UUID> {
    fun findByCloakIgnoreCase(cloak: String): Optional<RouterUser>
}
