/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.matchCardinality
import com.enigmastation.streampack.extensions.pluralize
import com.enigmastation.streampack.factoid.entity.FactoidAttribute
import com.enigmastation.streampack.factoid.model.FactoidAttributeType
import com.enigmastation.streampack.factoid.operation.GetFactoidOperation.NotEnoughArgumentsException
import com.enigmastation.streampack.factoid.service.FactoidService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class GetFactoidOperation(val factoidService: FactoidService) : RouterOperation() {
    class NotEnoughArgumentsException(s: String) : Exception(s)

    override fun canHandle(message: RouterMessage): Boolean {
        // don't even consider anything that doesn't start with "~"
        return message.content.trim().startsWith("~") && message.content.indexOf("=") == -1
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        try {
            val command = parseQuery(message.content)
            command?.let { cmd ->
                val data = factoidService.findSelectorWithArguments(cmd.selector)
                if (data.isEmpty) {
                    return null
                }
                // okay, we have a command.
                val (selector, arguments) = (data.get())
                // get all attributes; we may not need them, but the common case would be yes, we
                // do.
                val attributes = factoidService.findBySelector(selector)
                if (attributes.isNotEmpty()) {
                    return when (cmd.attribute) {
                        // forget mutates the data set, by removing ALL data about a selector.
                        FactoidAttributeType.FORGET -> {
                            factoidService.deleteSelector(cmd.selector)
                            null
                        }
                        // if the attribute is "unknown" - we need to build a response from the
                        // factoid's entire dataset.
                        FactoidAttributeType.UNKNOWN -> {
                            // okay, we need to get the available factoid attributes, ordered by
                            // type.
                            message.respondWith(
                                attributes.summarize(selector, factoidService, arguments)
                            )
                        }
                        // if the attribute is "info" - we need to tell the user what attributes are
                        // available.
                        FactoidAttributeType.INFO -> {
                            val lastAttribute = attributes.sortedBy { it.updateTimestamp }.first()

                            message.respondWith(
                                "The factoid for ${cmd.selector} has the following ${
                                    "attribute".pluralize(attributes)
                                }: " +
                                    attributes.buildAvailableAttributeList() +
                                    ", and was last modified at ${lastAttribute.updateTimestamp}" +
                                    if (lastAttribute.updatedBy != null) {
                                        " by ${lastAttribute.updatedBy}"
                                    } else {
                                        ""
                                    }
                            )
                        }
                        // otherwise, we look up the specific attribute and dump that out.
                        else -> {
                            val attribute =
                                attributes
                                    .filter { it.attributeType == cmd.attribute }
                                    .firstOrNull()
                            if (
                                attribute != null && (attribute.attributeValue ?: "").isNotEmpty()
                            ) {
                                val value =
                                    when (attribute.attributeType!!) {
                                        FactoidAttributeType.TEXT ->
                                            replaceParameters(
                                                selector,
                                                attribute.attributeValue!!,
                                                arguments
                                            )
                                        // we need to interpolate "seealso" values.
                                        FactoidAttributeType.SEEALSO ->
                                            renderSeeAlso(selector, factoidService, attribute)
                                        else -> attribute.attributeValue
                                    }
                                message.respondWith(
                                    attribute.attributeType!!.render(selector, value)
                                )
                            } else {
                                null
                            }
                        }
                    }
                }
            }
            return null
        } catch (e: NotEnoughArgumentsException) {
            return message.respondWith(e.message!!)
        }
    }

    fun parseQuery(input: String): GetFactoidCommand? {
        // Check if input starts with "~", which indicates a command
        if (!input.startsWith("~")) return null

        // Remove the leading "~"
        val selectorAttribute = input.substring(1).compress()

        // Split the selector-attribute part on the last '.'
        val lastDotIndex = selectorAttribute.lastIndexOf('.')

        val (selector, attribute) =
            if (lastDotIndex != -1) {
                val potentialAttribute = selectorAttribute.substring(lastDotIndex + 1).trim()
                if (potentialAttribute.lowercase() in FactoidAttributeType.knownAttributes.keys) {
                    // Split into selector and attribute
                    selectorAttribute.substring(0, lastDotIndex).trim() to potentialAttribute
                } else {
                    // Treat the entire part as the selector and default attribute to "unknown"
                    selectorAttribute to "unknown"
                }
            } else {
                // No dot found, so treat the whole as the selector and default attribute to
                // "unknown"
                selectorAttribute to "unknown"
            }
        val attr = FactoidAttributeType.knownAttributes[attribute] ?: FactoidAttributeType.UNKNOWN

        return GetFactoidCommand(selector, attr)
    }
}

fun List<FactoidAttribute>.buildAvailableAttributeList(): String {
    return this.filter { (it.attributeValue ?: "").isNotEmpty() }
        .map {
            val values =
                when (it.attributeType) {
                    FactoidAttributeType.URLS -> it.attributeValue!!.split(",")
                    FactoidAttributeType.TAGS -> it.attributeValue!!.split(",")
                    FactoidAttributeType.LANGUAGES -> it.attributeValue!!.split(",")
                    else -> listOf(it.attributeValue)
                }
            it.attributeType.toString().lowercase().matchCardinality(values)
        }
        .joinToStringWithAnd()
}

private fun replaceParameters(selector: String, value: String, arguments: List<String>): String {
    val regex = "\\$(\\d+)".toRegex()

    // Find all placeholders like $1, $2, etc.
    val placeholders = regex.findAll(value)

    // Track the highest placeholder number to ensure we have enough arguments
    val maxPlaceholder = placeholders.map { it.groupValues[1].toInt() }.maxOrNull()

    // Check if the arguments list is large enough
    if (maxPlaceholder != null && maxPlaceholder > arguments.size) {
        throw NotEnoughArgumentsException(
            "$selector: Not enough arguments to replace placeholders. Expected at least $maxPlaceholder but got ${arguments.size}."
        )
    }

    // Replace placeholders with the corresponding argument values
    return value.replace(regex) { matchResult ->
        val index = matchResult.groupValues[1].toInt() - 1
        if (index < arguments.size) arguments[index]
        else throw NotEnoughArgumentsException("$selector: Invalid argument index $index.")
    }
}

fun List<FactoidAttribute>.summarize(
    selector: String,
    factoidService: FactoidService,
    arguments: List<String>
): String {
    return this.sortedBy { it.attributeType?.ordinal }
        .filter { (it.attributeValue ?: "").isNotEmpty() }
        .map {
            val attr = it.attributeType!!
            val attribute =
                when (it.attributeType!!) {
                    FactoidAttributeType.TEXT ->
                        replaceParameters(selector, it.attributeValue!!, arguments)
                    FactoidAttributeType.SEEALSO -> renderSeeAlso(selector, factoidService, it)
                    else -> it.attributeValue
                }
            if (attribute!!.isNotEmpty()) {
                attr.render(selector, attribute)
            } else {
                ""
            }
        }
        .joinToString(" ")
        .compress()
}

fun renderSeeAlso(
    selector: String,
    factoidService: FactoidService,
    attribute: FactoidAttribute
): String {
    // we need to remove the selector from the attribute
    // value if it's there.
    return attribute.attributeValue!!
        .split(",")
        .toList()
        .filterNot { it.equals(selector, ignoreCase = true) }
        // add the tilde if it's a real factoid (and
        // it's not there already)
        .map {
            if (factoidService.findBySelector(it).isNotEmpty()) {
                "~${it.removePrefix("~")}"
            } else {
                it.removePrefix("~")
            }
        }
        .joinToString(",")
}
