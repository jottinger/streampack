/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.extensions

import java.net.URI
import java.net.URL
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils
import org.atteo.evo.inflector.English

fun String.compress(): String = this.trim().split(" ").filter { it.isNotEmpty() }.joinToString(" ")

fun List<String>.joinToStringWithAnd(): String {
    return when (this.size) {
        0 -> ""
        1 -> this[0]
        2 -> this.joinToString(" and ")
        else -> this.dropLast(1).joinToString(", ") + ", and " + this.last()
    }
}

@Deprecated("use pluralize() instead", replaceWith = ReplaceWith("pluralize(cardinality)"))
fun String.matchCardinality(cardinality: Collection<Any?>): String {
    return this.pluralize(cardinality)
}

fun String.endsWithPunctuation(): Boolean {
    val punctuationRegex = Pattern.compile("(.*)\\p{Punct}\$", Pattern.MULTILINE)
    return punctuationRegex.matcher(this).matches()
}

fun String.toURL(): URL = URI(this).toURL()

fun String.possessive(): String {
    return if (this.endsWith("s")) {
        "$this'"
    } else {
        "$this's"
    }
}

// for IRC, since this is done ALL THE TIME
fun String.isChannelReference() = this.trim().startsWith("#")

fun String.pluralize(cardinality: Collection<Any?>): String {
    var size = cardinality.size
    var base = this.singularForm()
    return if (size != 1) {
        base.pluralForm()
    } else {
        base
    }
}

fun String.pluralForm(): String {
    return English.plural(this)
}

fun String.singularForm(): String {
    val irregulars =
        mapOf(
            "children" to "child",
            "men" to "man",
            "women" to "woman",
            "mice" to "mouse",
            "geese" to "goose",
            "feet" to "foot",
            "teeth" to "tooth",
            "people" to "person",
            "oxen" to "ox",
            "indices" to "index",
            "matrices" to "matrix",
            "vertices" to "vertex",
            "data" to "datum",
            "criteria" to "criterion",
            "bacteria" to "bacterium",
            // Add more irregular forms as needed
        )
    val singularGreekForm = listOf("iris")

    // Check for irregular plural forms
    irregulars[this]?.let {
        return it
    }

    // Handle words ending with 'ies' -> 'y' (e.g., "babies" -> "baby")
    if (this.matches(Regex(".*[^aeiou]ies$"))) {
        return this.dropLast(3) + "y"
    }

    // Handle words ending with 'ves' -> 'f' or 'fe' (e.g., "wolves" -> "wolf")
    if (this.matches(Regex(".*(l|sh|[^aeiou])ves$"))) {
        return when {
            this.endsWith("lves") -> this.dropLast(3) + "f"
            this.endsWith("ves") -> this.dropLast(3) + "fe"
            else -> this.dropLast(3) + "f"
        }
    }

    // Handle words ending with 'oes', 'xes', 'ches', 'shes', 'ses' (remove 'es')
    if (this.matches(Regex(".*(ses|oes|xes|ches|shes|sses)$"))) {
        return this.dropLast(2)
    }

    // Handle regular plurals ending with 's' (e.g., "cats" -> "cat")
    if (this.endsWith("s") && this.length > 1 && !singularGreekForm.contains(this)) {
        return this.dropLast(1)
    }

    // Return the original word if no rules apply
    return this
}

fun String.htmlDecode() = StringEscapeUtils.unescapeHtml4(this)

fun String.htmlEncode() = StringEscapeUtils.escapeHtml4(this)

const val STANDARD_TIMEOUT = 50L

fun watchWithTimeout(thing: () -> Boolean, timeout: Int = 5000) {
    val start = System.currentTimeMillis()
    while (thing() == false && (System.currentTimeMillis() - start) < timeout) {
        Thread.sleep(STANDARD_TIMEOUT) // Avoid busy-waiting; check every 100ms
    }
    println("Delay: ${System.currentTimeMillis() - start} ms")
}

fun String.isPlural(): Boolean {
    // Words ending with -s are plural, except for those listed below.
    if (this.endsWith("s")) {
        return when {
            this.contains("x") && this.length > 2 -> false // Words like "axis" and "focus"
            this.contains("is") && this.length > 3 -> false // Words like "basis" and "fission"
            this.contains("tus") && this.length > 4 -> false // Words like "actus" and "cactus"
            else -> true
        }
    }

    // Words ending with -es are plural.
    if (this.endsWith("es")) {
        return true
    }

    // Words ending with -ie are singular.
    if (this.endsWith("ie")) {
        return false
    }

    // Words ending with -is are singular, except for those listed below.
    if (this.endsWith("is")) {
        return when {
            this.contains("basis") -> true // Special case: "basis" is plural
            else -> false
        }
    }

    // Words ending with -ment and -ence are singular.
    if (this.endsWith("ment") || this.endsWith("ence")) {
        return false
    }

    // Default to singular.
    return true
}
