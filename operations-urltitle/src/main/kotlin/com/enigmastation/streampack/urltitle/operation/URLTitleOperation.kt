/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.htmlDecode
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.pluralize
import com.enigmastation.streampack.extensions.possessive
import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.web.service.JsoupService
import com.enigmastation.streampack.web.service.OkHttpService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import java.util.regex.Pattern
import kotlin.text.Regex
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.springframework.stereotype.Service

@Service
class URLTitleOperation(var jsoupService: JsoupService, var okHttpService: OkHttpService) :
    RouterOperation(priority = 11) {
    override fun canHandle(message: RouterMessage): Boolean {
        return pattern.matcher(message.content).find()
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val foundUrls = findUrls(message.content)
        if (foundUrls.isEmpty()) return null

        val titles =
            foundUrls
                // ignore twitter for now! they parse... uh... poorly
                .filter { !it.contains("/x.com", true) }
                .filter { !it.contains("/twitter.com", true) }
                .mapNotNull { url ->
                    try {
                        url to getFinalURL(url)
                    } catch (_: Throwable) {
                        null
                    }
                }
                .mapNotNull { url ->
                    try {
                        logger.debug("url in message: {}, actual url: {}", url.first, url.second)
                        val doc = jsoupService.get(url.second)
                        url.first to (url.second to doc.title())
                    } catch (_: Throwable) {
                        null
                    }
                }
                .filter { it.second.second != null }
                .filter {
                    val similarity = calculateJaccardSimilarity(it.first, it.second.second)
                    logger.info(
                        "url: {}, title: {}, similarity {}",
                        it.first,
                        it.second.second,
                        similarity
                    )
                    similarity < 0.3
                }
                .map { it.first to it.second.second.htmlDecode() }
        return if (titles.isNotEmpty()) {
            val finalTitles = titles.map { entry -> "${entry.first} (\"${entry.second}\")" }
            message.respondWith(
                "The ${"title".pluralize(finalTitles)} for ${message.source!!.possessive()} ${
                    "url".pluralize(finalTitles)
                }: ${finalTitles.joinToStringWithAnd()}"
            )
        } else {
            null
        }
    }

    fun getFinalURL(url: String): String {
        val client = okHttpService.client(false)
        var currentUrl = url
        var response: Response

        do {
            val request = okHttpService.buildRequest(currentUrl)

            response = client.newCall(request).execute()

            // If the response is a redirect, get the "Location" header
            val locationHeader = response.header("Location")
            if (locationHeader != null) {
                currentUrl = locationHeader
            }
            response.closeQuietly()
        } while (response.isRedirect)

        return currentUrl
    }

    companion object {
        val pattern = Pattern.compile("(?:(http|https)(://))")

        fun findUrls(content: String): List<String> {
            // okay, let's find all of the HTTP-ish URLs.
            return content
                .compress()
                .split(" ")
                .filter { pattern.matcher(it.lowercase()).find() }
                .toList()
        }

        fun tokenize(text: String): Set<String> {
            return text
                .lowercase()
                .split("\\W+".toRegex()) // Split by any non-word character
                .filter { it.isNotEmpty() }
                .toSet()
        }

        fun extractHost(url: String) = url.toURL().host

        fun cleanUrl(url: String): String =
            url.replace("https://", "")
                .replace("http://", "")
                .replace("www", "")
                .replace(".", " ")
                .replace("-", " ")
                .replace("index", "")
                .replace("html", "")
                .replace("htm", "")
                .replace("/", " ")
                .replace(Regex("[0-9]+"), "")
                .compress()

        fun calculateJaccardSimilarity(url: String, title: String): Double {
            val cleanUrl = cleanUrl(url)

            val urlWords = tokenize(cleanUrl)
            val titleWords = tokenize("$title ${cleanUrl(extractHost(url))}")

            val intersection = urlWords.intersect(titleWords).size
            val union = urlWords.union(titleWords).size

            return if (union == 0) 0.0 else intersection.toDouble() / union.toDouble()
        }
    }
}
