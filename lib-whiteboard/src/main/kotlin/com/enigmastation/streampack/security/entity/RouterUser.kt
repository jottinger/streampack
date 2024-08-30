/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.security.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "users",
    indexes =
        [
            Index("username", columnList = "username", unique = true),
            Index("cloak", columnList = "cloak", unique = false)
        ]
)
class RouterUser(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    var username: String? = null,
    var password: String? = null,
    var enabled: Boolean = false,
    var cloak: String? = null,
    @Column() var roles: String? = null,
    var createTimestamp: OffsetDateTime? = null,
    var updateTimestamp: OffsetDateTime? = null
) {
    @PreUpdate
    fun updateTimestamps() {
        updateTimestamp = OffsetDateTime.now()
        if (roles == null) {
            roles = "USER"
        }
    }

    @PrePersist
    fun checkCreationDate() {
        if (createTimestamp == null) {
            createTimestamp = OffsetDateTime.now()
        }
        updateTimestamps()
    }

    fun getRoles(): List<String> {
        return (roles ?: "").split(",").map { it.lowercase() }
    }

    fun hasRole(role: String): Boolean {
        return getRoles().contains(role.lowercase())
    }

    override fun toString(): String {
        return "RouterUser[username=$username, enabled=$enabled, cloak=$cloak, roles=$roles]"
    }
}
