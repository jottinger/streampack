/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rss.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** TODO we need to find a better way than relying on external sites for these tests. */
@SpringBootTest
class RSSFeedServiceTests {
    @Autowired lateinit var service: RSSFeedService

    // TODO set up mocks so we can test this stuff out again

    //    @Test
    //    fun `read feed`() {
    //        println(service.findFeedFromSite("https://enigmastation.com/"))
    //    }
    //
    //    @Test
    //    fun `read substack url`() {
    //        val url = service.findFeedFromSite("https://hwfo.substack.com/")
    //        println(url)
    //        assertEquals("https://hwfo.substack.com/feed", url)
    //        // println(service.getFeed(url))
    //    }
}
