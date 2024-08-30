/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.text.isEmpty
import org.slf4j.LoggerFactory

abstract class NamedService(name: String? = null, timeout: Duration? = null) {
    val logger = LoggerFactory.getLogger(this::class.java)
    var name: String = name ?: ""
        get() {
            return if (field.isEmpty()) {
                field = this::class.simpleName ?: "UnnamedService"
                field
            } else {
                field
            }
        }

    var timeout: Duration = timeout ?: Duration.of(5, ChronoUnit.SECONDS)

    open fun canHandle(message: RouterMessage): Boolean = true

    open fun receive(message: RouterMessage): RouterMessage? {
        return null
    }

    open fun description(): String = "No short description for $name"

    open fun longDescription(): String = "No long description for $name"
}
