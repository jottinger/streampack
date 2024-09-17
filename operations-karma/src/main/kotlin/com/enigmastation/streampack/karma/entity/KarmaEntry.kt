/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "karma_entries",
    indexes =
        [
            Index(name = "idx_selector", columnList = "selector,createTimestamp"),
            Index(name = "idx_createTimestamp", columnList = "createTimestamp")
        ]
)
class KarmaEntry(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false) var selector: String? = null,
    @Column(nullable = false) var increment: Int? = null,
    @Column(nullable = false) var createTimestamp: OffsetDateTime? = null,
    @Column(nullable = true) var comment: String? = null
) {
    @PrePersist
    fun updateCreateTimestamp() {
        // allows us to test with this null check. Otherwise, we have to MUTATE the value for
        // testing.
        if (createTimestamp == null) {
            createTimestamp = OffsetDateTime.now()
        }
    }

    override fun toString(): String {
        return "KarmaEntry[id='$id',selector='$selector',increment=$increment,createTimestamp=$createTimestamp,comment=$comment]"
    }
}
