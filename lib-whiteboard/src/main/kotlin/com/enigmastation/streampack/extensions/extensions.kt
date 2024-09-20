/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.extensions

import java.net.URI
import java.net.URL
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils

fun String.compress(): String = this.trim().split(" ").filter { it.isNotEmpty() }.joinToString(" ")

fun List<String>.joinToStringWithAnd(): String {
    return when (this.size) {
        0 -> ""
        1 -> this[0]
        2 -> this.joinToString(" and ")
        else -> this.dropLast(1).joinToString(", ") + ", and " + this.last()
    }
}

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
fun String.isChannelReference() = this.trim().startsWith("#'")

fun String.pluralize(cardinality: Collection<Any?>): String =
    if (cardinality.size == 1) {
        this.removeSuffix("s")
    } else {
        "${this.removeSuffix("s")}s"
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
