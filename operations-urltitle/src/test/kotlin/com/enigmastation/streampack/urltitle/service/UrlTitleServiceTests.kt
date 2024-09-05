/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.service

import com.enigmastation.streampack.urltitle.repository.IgnoredHostRepository
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UrlTitleServiceTests {
    @Autowired lateinit var service: UrlTitleService
    @Autowired lateinit var repository: IgnoredHostRepository

    @ParameterizedTest
    @MethodSource("urlSimilarityInputs")
    fun `url similarity`(url: String, title: String, minimumSimilarity: Double) {
        // println(URLTitleOperation.cleanUrl(url))
        // println(URLTitleOperation.calculateJaccardSimilarity(url, title))
        assertTrue(service.calculateJaccardSimilarity(url, title) >= minimumSimilarity)
    }

    @Test
    fun `17-remove-this-test`() {
        repository.findAll().forEach { println(it.hostName) }
    }

    @ParameterizedTest
    @MethodSource("ignoredHostNameCandidates")
    fun `17-check hostnames`(url: String, ignored: Boolean) {
        assertEquals(ignored, service.isIgnoredHost(url))
    }

    @Test
    fun `17-test-url-filters`() {
        var foundUrls = listOf("https://twitter.com", "http://x.com/", "https://enigmastation.com")
        val results = service.filteredUrls(foundUrls)
        println(results)
        assertEquals(1, results.size)
        assertEquals("https://enigmastation.com", results.get(0))
    }

    companion object {
        @JvmStatic
        fun ignoredHostNameCandidates(): Stream<Arguments> =
            Stream.of(
                Arguments.of("https://x.com/kjhaskdh", true),
                Arguments.of("https://foo.x.com/kjhaskdh", false)
            )

        @JvmStatic
        fun urlSimilarityInputs() =
            Stream.of(
                Arguments.of(
                    "http://foo.com/bar/baz.html",
                    "never gonna give you up, never gonna let you down",
                    0.0
                ),
                Arguments.of("http://foo.com/bar/baz.html", "bar baz (foo.com)", 0.9),
                Arguments.of("http://foo.com/bar/baz.html", "bar baz", 0.4),
                Arguments.of(
                    "https://enigmastation.com/2023/09/06/weechat-on-osx/",
                    "Weechat on OSX",
                    0.370
                ),
                Arguments.of(
                    "https://enigmastation.com/2023/09/06/weechat/",
                    "Weechat (enigmastation com)",
                    0.49
                ),
                Arguments.of(
                    "https://www.cnn.com/2024/08/19/business/harley-davidson-dei-john-deere-tractor-supply/index.html",
                    "Harley-Davidson is dropping diversity initiatives after right-wing anti-DEI campaign | CNN Business",
                    0.31
                )
            )
    }
}
