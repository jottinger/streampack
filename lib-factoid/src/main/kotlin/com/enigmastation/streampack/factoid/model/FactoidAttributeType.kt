/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.model

import com.enigmastation.streampack.extensions.endsWithPunctuation
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.pluralize

enum class FactoidAttributeType(val mutable: Boolean = true) {
    TEXT {
        override fun doRender(selector: String, value: String): String {
            val data =
                if (value.startsWith("<reply>", true)) {
                    value.substring("<reply>".length)
                } else {
                    "${selector.lowercase()} is $value"
                }
            return if (!data.endsWithPunctuation()) {
                "${data}."
            } else {
                data
            }
        }
    },
    URLS {
        override fun doRender(selector: String, value: String): String {
            return renderPluralValue("URL", value)
        }
    },
    TAGS {
        override fun doRender(selector: String, value: String): String {
            return renderPluralValue("Tag", value)
        }
    },
    LANGUAGES {
        override fun doRender(selector: String, value: String): String {
            return renderPluralValue("Language", value)
        }
    },
    TYPE {
        override fun doRender(selector: String, value: String): String {
            return renderPluralValue("Type", value)
        }
    },
    SEEALSO {
        override fun doRender(selector: String, value: String): String {
            val values = value.split(",")
            return "See also: ${values.joinToStringWithAnd()}"
        }
    },
    FORGET(false),
    UNKNOWN(false),
    INFO(false);

    open fun doRender(selector: String, value: String): String {
        return value
    }

    fun render(selector: String, value: String?) =
        if (value != null) {
            doRender(selector, value)
        } else ""

    companion object {
        fun renderPluralValue(name: String, value: String): String {
            val values = value.split(",")
            return "${name.pluralize( values)}: ${values.joinToStringWithAnd()}"
        }

        val knownAttributes: Map<String, FactoidAttributeType>

        init {
            val map = mutableMapOf<String, FactoidAttributeType>()
            FactoidAttributeType.entries.forEach {
                val name = it.name.lowercase()
                map[name] = it
                if (name.endsWith("s", true)) {
                    map[name.removeSuffix("s")] = it
                }
            }
            knownAttributes = map
        }
    }
}
