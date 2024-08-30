/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class HelpOperation(val context: ApplicationContext) : RouterOperation() {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.compress().startsWith("~help")
    }

    override fun description(): String {
        return "Provides help for commands. Use the name provided by \"~list operations\", like so: \"~help ${this.name}\""
    }

    override fun longDescription(): String {
        return super.longDescription()
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val operation = message.content.compress().removePrefix("~help").compress().split(" ")[0]
        return if (operation.isEmpty()) {
            message.respondWith(
                ("Try \"~list operations\" to get a list of operations; then, \"~help [operation name]\"")
            )
        } else {
            val command =
                context.getBeansOfType(RouterOperation::class.java).values.find {
                    it.name == operation
                }
            if (command != null) {
                if (command.description().compress().isNotEmpty()) {
                    message.respondWith("${command.name}: ${command.description()}")
                } else {
                    message.respondWith("${command.name} has no help text.")
                }
            } else {
                null
            }
        }
    }
}
