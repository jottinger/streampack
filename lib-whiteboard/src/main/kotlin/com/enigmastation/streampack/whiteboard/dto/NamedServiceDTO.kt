/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.dto

data class NamedServiceDTO(
    val name: String,
    val description: String,
    val longDescription: String,
    val priority: Int
)
