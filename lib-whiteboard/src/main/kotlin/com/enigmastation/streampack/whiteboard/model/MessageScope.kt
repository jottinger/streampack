/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

enum class MessageScope {
    PUBLIC,
    PRIVATE,
    INTERNAL,
    // this allows operations, for example, to set messages as terminal so that they're silent BUT
    // no longer processed.
    TERMINAL
}
