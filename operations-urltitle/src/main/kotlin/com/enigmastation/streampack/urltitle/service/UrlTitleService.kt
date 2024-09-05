/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.service

import com.enigmastation.streampack.urltitle.entity.IgnoredHost
import com.enigmastation.streampack.urltitle.operation.URLTitleOperation.Companion.cleanUrl
import com.enigmastation.streampack.urltitle.operation.URLTitleOperation.Companion.extractHost
import com.enigmastation.streampack.urltitle.operation.URLTitleOperation.Companion.tokenize
import com.enigmastation.streampack.urltitle.operation.UrlTitleConfiguration
import com.enigmastation.streampack.urltitle.repository.IgnoredHostRepository
import com.enigmastation.streampack.web.service.JsoupService
import com.enigmastation.streampack.web.service.OkHttpService
import java.net.URI
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UrlTitleService : InitializingBean {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired lateinit var okHttpService: OkHttpService

    @Autowired lateinit var jsoupService: JsoupService

    @Autowired lateinit var ignoredHostRepository: IgnoredHostRepository

    @Autowired lateinit var urlTitleConfiguration: UrlTitleConfiguration

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

    fun getTitle(url: Pair<String, String>): Pair<String, Pair<String, String?>>? {
        return try {
            logger.debug("url in message: {}, actual url: {}", url.first, url.second)
            val doc = jsoupService.get(url.second)
            url.first to (url.second to doc.title())
        } catch (_: Throwable) {
            null
        }
    }

    fun calculateJaccardSimilarity(url: String, title: String): Double {
        val cleanUrl = cleanUrl(url)

        val urlWords = tokenize(cleanUrl)
        val titleWords = tokenize("$title ${cleanUrl(extractHost(url))}")

        val intersection = urlWords.intersect(titleWords).size
        val union = urlWords.union(titleWords).size

        return if (union == 0) 0.0 else intersection.toDouble() / union.toDouble()
    }

    @Transactional
    fun isIgnoredHost(hostName: String): Boolean {
        val name =
            try {
                val host = URI(hostName).host
                host ?: hostName
            } catch (_: Throwable) {
                hostName
            }
        return ignoredHostRepository
            .findByHostNameIgnoreCaseStartsWith("${name.normalizeHostname()}")
            .isPresent
    }

    @Transactional
    fun saveIgnoredHost(name: String) {
        if (!isIgnoredHost(name)) {
            ignoredHostRepository.save(IgnoredHost(hostName = name.normalizeHostname()))
        }
    }

    @Transactional
    override fun afterPropertiesSet() {
        urlTitleConfiguration.defaultIgnoredHosts.forEach { hostName -> saveIgnoredHost(hostName) }
    }

    @Transactional
    fun addIgnoredHost(hostName: String) {
        saveIgnoredHost(hostName)
    }

    @Transactional
    fun deleteIgnoredHost(hostName: String) {
        if (isIgnoredHost(hostName)) {
            ignoredHostRepository.delete(
                ignoredHostRepository.findByHostNameIgnoreCaseStartsWith(hostName).get()
            )
        }
    }

    @Transactional
    fun findBannedHosts(): List<String> =
        ignoredHostRepository.findAll().mapNotNull { it.hostName }.toList()

    @Transactional fun findAll() = ignoredHostRepository.findAll()

    @Transactional
    fun isIgnored(url: String): Boolean {
        // scan for matches from ignored hosts in the url
        return ignoredHostRepository
            .findAll()
            .mapNotNull { it.hostName }
            .any { url.contains(it, true) }
    }

    @Transactional
    fun filteredUrls(foundUrls: List<String>): List<String> {
        return foundUrls
            // ignore twitter for now! they parse... uh... poorly
            .filter { !isIgnored(it) }
    }
}

private fun String.normalizeHostname(): String = "/${this.removePrefix("/")}"
