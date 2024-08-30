/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.web.service

import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service

@Service
class JsoupService {
    fun get(url: String): Document = Jsoup.connect(url).userAgent(USER_AGENT).get()

    fun get(url: URL): Document = get(url.toString())

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
    }
}
