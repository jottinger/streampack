/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class IgnoredHost(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    var hostName: String? = null
)
