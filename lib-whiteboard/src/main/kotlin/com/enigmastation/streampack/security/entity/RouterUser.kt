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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

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
    private var username: String? = null,
    private var password: String? = null,
    var enabled: Boolean = false,
    var cloak: String? = null,
    @Column() var roles: String? = null,
    var createTimestamp: OffsetDateTime? = null,
    var updateTimestamp: OffsetDateTime? = null
) : UserDetails {
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

    @Deprecated(
        message = "use getAuthorities() instead",
        replaceWith = ReplaceWith("getAuthorities()")
    )
    fun getRoles(): List<String> {
        return authorities.map { it.authority }
    }

    fun hasRole(role: String): Boolean {
        return authorities.contains(SimpleGrantedAuthority("ROLE_${role}".uppercase()))
    }

    override fun toString(): String {
        return "RouterUser[username=$username, enabled=$enabled, cloak=$cloak, roles=$roles]"
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return (roles ?: "").split(",").map { SimpleGrantedAuthority("ROLE_$it") }
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getUsername(): String? {
        return username
    }
}
