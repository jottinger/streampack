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
    var user: RouterUser? = null
    var messageSource: MessageSource = MessageSource.UNKNOWN

    fun build(): RouterMessage =
        RouterMessage(
            content = content,
            target = target,
            source = source,
            server = server,
            context = context,
            operation = operation,
            scope = scope,
            messageSource = messageSource,
            process = process,
            cloak = cloak,
            user = user
        )
}

fun routerMessage(init: RouterMessageBuilder.() -> Unit): RouterMessage {
    val builder = RouterMessageBuilder()
    builder.init()
    return builder.build()
}
