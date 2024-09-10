/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
class Factoid(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false, unique = true) var selector: String? = null,
    @OneToMany(mappedBy = "selector", cascade = [CascadeType.DETACH])
    var attributes: Set<FactoidAttribute> = setOf(),
    var locked: Boolean = false,
    var updatedBy: String? = null,
    var createTimestamp: OffsetDateTime? = null,
    var updateTimestamp: OffsetDateTime? = null
) {
    @PrePersist
    fun updateCreateTimestamp() {
        createTimestamp = OffsetDateTime.now()
        updateTimestamps()
    }

    @PreUpdate
    fun updateTimestamps() {
        selector = selector?.lowercase()
        updateTimestamp = OffsetDateTime.now()
    }
}
