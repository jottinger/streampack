/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ListOperationsOperation(val context: ApplicationContext) : RouterOperation() {
    override fun canHandle(message: RouterMessage): Boolean {

        return message.content.compress().equals("~list operations", true)
    }

    override fun description(): String {
        return "Lists all of the currently installed operations, invoked by \"~list operations\""
    }

    override fun longDescription(): String {
        return super.longDescription()
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val operations = context.getBeansOfType(RouterOperation::class.java).values

        return message.respondWith(
            "Installed operations are: ${operations.map { it.name }.joinToStringWithAnd()}"
        )
    }
}
