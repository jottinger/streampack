/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class URLTitleOperationTests {
    @ParameterizedTest
    @MethodSource("urlSimilarityInputs")
    fun `url similarity`(url: String, title: String, minimumSimilarity: Double) {
        // println(URLTitleOperation.cleanUrl(url))
        // println(URLTitleOperation.calculateJaccardSimilarity(url, title))
        assertTrue(URLTitleOperation.calculateJaccardSimilarity(url, title) >= minimumSimilarity)
    }

    companion object {
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
