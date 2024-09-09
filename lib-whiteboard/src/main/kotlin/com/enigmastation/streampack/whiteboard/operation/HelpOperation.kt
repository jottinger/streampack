/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import com.github.mpe85.grampa.parser.Parser
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class HelpOperation(val context: ApplicationContext) : RouterOperation(), InitializingBean {
    lateinit var parser: Parser<String>

    override fun canHandle(message: RouterMessage): Boolean {
        return parser.run(message.content).matched
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
        val operation = parser.run(message.content).stackTop

        return if (operation.isNullOrEmpty()) {
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

    override fun afterPropertiesSet() {
        var operations =
            context.getBeansOfType(RouterOperation::class.java).values.filterNotNull().toList()
        parser = HelpOperationGrammar.parser(operations)
    }
}
