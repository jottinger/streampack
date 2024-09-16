/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import com.enigmastation.streampack.security.entity.RouterUser

class RouterMessageBuilder {
    var content: String = ""
    var target: String? = null
    var source: String? = null
    var context: String? = null
    var operation: String? = null
    var server: String? = null
    var scope: MessageScope = MessageScope.PRIVATE
    var process: Boolean = true
    var cloak: String? = null
    var messageSource: MessageSource = MessageSource.UNKNOWN
    var user: RouterUser? = null

    fun build(): RouterMessage =
        RouterMessage(
            content,
            target,
            source,
            server,
            context,
            operation,
            scope,
            messageSource,
            process,
            cloak,
            user = user
        )
}

fun routerMessage(init: RouterMessageBuilder.() -> Unit): RouterMessage {
    val builder = RouterMessageBuilder()
    builder.init()
    return builder.build()
}
