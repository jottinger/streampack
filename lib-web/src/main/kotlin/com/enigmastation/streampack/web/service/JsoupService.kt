/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.web.service

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.net.URL
import java.util.concurrent.TimeUnit
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JsoupService() {
    @Autowired lateinit var okHttpService: OkHttpService

    var cache: LoadingCache<String, Document> =
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(
                object : CacheLoader<String, Document>() {
                    override fun load(key: String): Document {
                        return loadUrl(key)
                    }
                }
            )

    private fun loadUrl(url: String): Document {
        val content = okHttpService.getUrl(url)
        return Jsoup.parse(content)
    }

    final fun get(url: String): Document = cache.get(url)

    final fun get(url: URL): Document = cache.get(url.toString())
}
