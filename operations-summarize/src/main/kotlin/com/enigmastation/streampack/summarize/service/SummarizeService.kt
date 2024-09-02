/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summarize.service

import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.summarize.model.Summary
import com.enigmastation.streampack.web.service.JsoupService
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SummarizeService() {

    @Autowired lateinit var builder: ChatClient.Builder

    @Autowired lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var jsoupService: JsoupService

    fun summarizeURL(url: URL): Summary {
        try {
            val document = jsoupService.get(url)

            var chatClient = builder.build()
            val query =
                """
                Summarize this content succinctly, in no more than three sentences:
                 
                ```
                # ${document.title()}
                
                ${document.text()}
                ```
                                
                Do not include the title or the author unless it's particularly significant 
                to the relevance of the content.
                                                
                Additionally, classify the content into a list 
                of categories, such as 'programming', 'rust', 'javascript', 'java', 
                'politics', 'news', 'physics', and the like,
                with the strongest categorizations coming first and the weakest coming last, 
                up to a maximum of five categories, but don't include categories that are 
                not represented by the source text.
                
                Your response should be in JSON format.
                If no summary is possible due to errors or not enough content, 
                set the summary to the literal text value "no summary possible" and the categories to an empty list.
                The data structure for the JSON should match this Kotlin class: com.enigmastation.streampack.summarize.model.Summary
                Do not include any explanations, only provide a RFC8259 compliant JSON response 
                following this format without deviation.
                """
                    .trimIndent()
            var response = chatClient.prompt().user(query).call().content()
            return objectMapper.readValue(response, Summary::class.java)
        } catch (e: Exception) {
            throw e
        }
    }

    fun summarizeURL(url: String): Summary {
        return summarizeURL(url.toURL())
    }
}
