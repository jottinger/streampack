/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.entity

import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    indexes =
        [
            Index(name = "factoid_attr", columnList = "selector,attribute_type", unique = true),
            Index(name = "factoid_selector", columnList = ("selector"), unique = false)
        ]
)
class FactoidAttribute(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false) var selector: String? = null,
    @Enumerated(EnumType.STRING) var attributeType: FactoidAttributeType? = null,
    @Column(length = 512) var attributeValue: String? = null,
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
