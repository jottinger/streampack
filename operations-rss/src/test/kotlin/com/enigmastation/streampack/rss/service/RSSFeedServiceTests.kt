/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RSSFeedServiceTests {
    @Autowired lateinit var service: RSSFeedService
    val homepage = this::class.java.getResource("/enigmastation.html")!!.readText()
    val rss = this::class.java.getResource("/enigmastation.rss")!!.readText()

    @Test
    fun `read feed`() {
        val mockWebServer = MockWebServer()
        val dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return when (request.path) {
                        "/" -> MockResponse().setResponseCode(200).setBody(homepage)
                        "/feed/" -> MockResponse().setResponseCode(200).setBody(rss)
                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()
        val url = mockWebServer.url("/").toString()

        assertEquals("https://enigmastation.com/feed/", service.findFeedFromSite(url))
        val feed = service.readFeed("${url}feed/")
        assertNotNull(feed)
        assertEquals(10, feed.entries.size)
    }
}
