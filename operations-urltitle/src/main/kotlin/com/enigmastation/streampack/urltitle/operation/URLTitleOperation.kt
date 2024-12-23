/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.htmlDecode
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.pluralize
import com.enigmastation.streampack.extensions.possessive
import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.urltitle.service.UrlTitleService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import kotlin.text.Regex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class URLTitleOperation() : RouterOperation(priority = 91) {
    @Autowired lateinit var urlTitleService: UrlTitleService
    @Autowired lateinit var configuration: UrlTitleConfiguration

    override fun canHandle(message: RouterMessage): Boolean {
        return if (configuration.services.contains(message.messageSource)) {
            val parser = UrlTitleGrammar.parser()
            var result = parser.run(message.content)
            return result.stack.isNotEmpty()
        } else {
            false
        }
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val parser = UrlTitleGrammar.parser()
        var result = parser.run(message.content)
        val foundUrls = result.stack.toList()

        val titles =
            urlTitleService
                .filteredUrls(foundUrls)
                .mapNotNull { url ->
                    try {
                        url to urlTitleService.getFinalURL(url)
                    } catch (_: Throwable) {
                        null
                    }
                }
                .mapNotNull { url -> urlTitleService.getTitle(url) }
                .filter { it.second.second != null }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as Pair<String, Pair<String, String>>
                }
                .filter {
                    val similarity =
                        urlTitleService.calculateJaccardSimilarity(it.first, it.second.second)
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

    companion object {

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
    }
}
