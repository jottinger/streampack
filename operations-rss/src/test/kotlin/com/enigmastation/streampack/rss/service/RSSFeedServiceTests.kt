/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.service

import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.rss.entity.RSSEntry
import com.enigmastation.streampack.summary.dto.Summary
import com.enigmastation.streampack.summary.service.SummarizeService
import java.net.URL
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
                        else -> {
                            println(request.path)
                            MockResponse().setResponseCode(404)
                        }
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

    @Test
    fun `test summarize service`() {
        var mockedService =
            object : SummarizeService() {
                override fun summarizeURL(url: URL): Summary {
                    var summary = Summary()
                    summary.summary = "this is a test"
                    summary.categories = listOf("test")
                    return summary
                }

                override fun summarizeURL(url: String): Summary {
                    return super.summarizeURL(url.toURL())
                }
            }
        var oldSummaryService = service.summarizeService
        try {
            service.summarizeService = mockedService
            val mockWebServer = MockWebServer()
            val dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return when (request.path) {
                            "/content" ->
                                MockResponse()
                                    .setResponseCode(200)
                                    .setBody(
                                        """<html><body>This is some test content!</body></html>"""
                                    )
                            else -> {
                                println(request.path)
                                MockResponse().setResponseCode(404)
                            }
                        }
                    }
                }
            mockWebServer.dispatcher = dispatcher
            mockWebServer.start()
            val url = mockWebServer.url("/content").toString()
            var entry = RSSEntry(title = "test", url = url.toURL())
            entry = service.rssEntryRepository.save(entry)
            service.summarizeSingleEntry()
            var e = service.rssEntryRepository.findByUrl(url.toURL()).orElseThrow()
            //            assertTrue(e.summarized!!)
            //            assertEquals("this is a test", e.llmSummary)
        } finally {
            service.summarizeService = oldSummaryService
        }
    }
}
