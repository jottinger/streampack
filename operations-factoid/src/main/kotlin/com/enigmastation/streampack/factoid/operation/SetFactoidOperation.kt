/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.factoid.entity.FactoidAttribute
import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import com.enigmastation.streampack.factoid.service.FactoidService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class SetFactoidOperation(val factoidService: FactoidService) : RouterOperation() {
    override fun canHandle(message: RouterMessage): Boolean {
        // don't even consider anything that doesn't start with "~"
        return message.content.trim().startsWith("~") &&
            message.content.removePrefix("~").indexOf("=") > 1
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        val command = parseInput(message.content)
        command?.let { cmd ->
            val factoid =
                factoidService
                    .findBySelectorAndAttributeType(cmd.selector, cmd.attribute)
                    .orElseGet {
                        FactoidAttribute(selector = cmd.selector, attributeType = cmd.attribute)
                    }
            factoid.attributeValue = cmd.value
            factoid.updatedBy = message.source
            factoidService.save(factoid)
            return message.copy(scope = MessageScope.TERMINAL)
        }
        // we don't emit messages on updates. It would be nice to handle errors, though...
        return null
    }

    fun parseInput(input: String): SetFactoidCommand? {
        // Check if input starts with "~", which indicates a command
        if (!input.startsWith("~")) return null

        // Remove the leading "~"
        val commandPart = input.substring(1)

        // Split the input into selector-attribute and value
        val parts = commandPart.split("=")
        if (parts.size != 2) return null // Invalid format if there's not exactly one '='

        var selectorAttribute = parts[0].compress()
        val value = parts[1].compress()

        // Split the selector-attribute part on the last '.'
        val lastDotIndex = selectorAttribute.lastIndexOf('.')

        val (selector, attribute) =
            if (lastDotIndex != -1) {
                val potentialAttribute = selectorAttribute.substring(lastDotIndex + 1).trim()
                if (potentialAttribute.lowercase() in FactoidAttributeType.knownAttributes.keys) {
                    // Split into selector and attribute
                    selectorAttribute.substring(0, lastDotIndex).trim() to potentialAttribute
                } else {
                    // Treat the entire part as the selector and default attribute to "text"
                    selectorAttribute to "text"
                }
            } else {
                // No dot found, so treat the whole as the selector and default attribute to "text"
                selectorAttribute to "text"
            }
        val attr = FactoidAttributeType.knownAttributes[attribute] ?: FactoidAttributeType.TEXT

        return if (attr.mutable) {
            SetFactoidCommand(selector, attr, value)
        } else null
    }
}
